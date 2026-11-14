package com.zhmindnotes.service;

import com.zhmindnotes.common.BusinessException;
import com.zhmindnotes.controller.dto.NoteSaveDTO;
import com.zhmindnotes.controller.dto.SegmentDTO;
import com.zhmindnotes.dao.*;
import com.zhmindnotes.model.Note;
import com.zhmindnotes.model.NoteSegment;
import com.zhmindnotes.service.ai.AiOrganizeService;
import com.zhmindnotes.service.ai.AiResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NoteService {

    private final NoteDao noteDao;
    private final SegmentDao segmentDao;
    private final LikeDao likeDao;
    private final ContributionDao contributionDao;
    private final AiOrganizeService aiService;
    private final JdbcTemplate jdbc;

    public NoteService(NoteDao noteDao, SegmentDao segmentDao, LikeDao likeDao,
                       ContributionDao contributionDao, AiOrganizeService aiService,
                       JdbcTemplate jdbc) {
        this.noteDao = noteDao;
        this.segmentDao = segmentDao;
        this.likeDao = likeDao;
        this.contributionDao = contributionDao;
        this.aiService = aiService;
        this.jdbc = jdbc;
    }

    /** 公开列表：课程分类过滤 + 关键词 + 阅读量/点赞量/最新排序 */
    public List<Note> listPublic(Long courseId, String sort, String keyword) {
        return noteDao.listPublic(courseId, sort, keyword);
    }

    public List<Note> listMine(Long userId) {
        List<Note> list = noteDao.listMine(userId);
        list.forEach(n -> n.setLiked(likeDao.noteLiked(n.getId(), userId)));
        return list;
    }

    /** 打开详情：阅读量 +1，填充片段与当前用户点赞状态 */
    public Note openDetail(Long noteId, Long userId) {
        Note note = noteDao.findDetailById(noteId);
        if (note == null) {
            throw new BusinessException(404, "笔记不存在或已被删除");
        }
        // 私有笔记仅作者本人可访问
        if (Integer.valueOf(0).equals(note.getShareStatus()) && !note.getUserId().equals(userId)) {
            throw new BusinessException(403, "该笔记为私有，仅作者本人可查看");
        }
        noteDao.incrementView(noteId);
        note.setViewCount(note.getViewCount() == null ? 1 : note.getViewCount() + 1);
        note.setSegments(segmentDao.listByNoteId(noteId));
        note.setLiked(userId != null && likeDao.noteLiked(noteId, userId));
        return note;
    }

    public Note getDetailWithoutView(Long noteId, Long userId) {
        Note note = noteDao.findDetailById(noteId);
        if (note == null) throw new BusinessException(404, "笔记不存在或已被删除");
        note.setSegments(segmentDao.listByNoteId(noteId));
        note.setLiked(userId != null && likeDao.noteLiked(noteId, userId));
        return note;
    }

    /** 新建笔记：保存主表 + 多格式片段，随后触发 AI 整理 */
    @Transactional
    public Long createNote(Long userId, NoteSaveDTO dto) {
        validateSave(dto);
        Long noteId = noteDao.insert(userId, dto.getCourseId(), dto.getTitle().trim(),
                dto.getShareStatus() == null ? 1 : dto.getShareStatus());
        saveSegments(noteId, dto.getSegments());
        runAiOrganize(noteId, dto.getTitle(), dto.getSegments());
        return noteId;
    }

    /** 手动重新整理（详情页“AI 重新整理”按钮） */
    @Transactional
    public Note refreshAi(Long noteId, Long userId) {
        assertOwner(noteId, userId);
        Note note = noteDao.findDetailById(noteId);
        List<NoteSegment> segments = segmentDao.listByNoteId(noteId);
        String fullText = segments.stream()
                .map(s -> {
                    if ("TEXT".equals(s.getSegType())) return s.getContentText();
                    return s.getCaption();
                }).collect(Collectors.joining("\n"));
        persistAiResult(noteId, note.getTitle(), fullText);
        return getDetailWithoutView(noteId, userId);
    }

    @Transactional
    public void changeShare(Long noteId, Long userId, Integer shareStatus) {
        assertOwner(noteId, userId);
        noteDao.updateShareStatus(noteId, shareStatus);
    }

    @Transactional
    public void deleteNote(Long noteId, Long userId) {
        assertOwner(noteId, userId);
        segmentDao.deleteByNoteId(noteId);
        jdbc.update("DELETE FROM contribution WHERE note_id = ?", noteId);
        jdbc.update("DELETE FROM contribution_like WHERE contribution_id IN (SELECT id FROM (SELECT id FROM contribution WHERE note_id = ?) t)", noteId);
        jdbc.update("DELETE FROM note_like WHERE note_id = ?", noteId);
        jdbc.update("DELETE FROM ai_log WHERE note_id = ?", noteId);
        noteDao.deleteById(noteId);
    }

    /** 点赞 / 取消点赞切换 */
    public Map<String, Object> toggleLike(Long noteId, Long userId) {
        if (noteDao.findDetailById(noteId) == null) {
            throw new BusinessException(404, "笔记不存在");
        }
        boolean liked;
        if (likeDao.noteLiked(noteId, userId)) {
            likeDao.removeNoteLike(noteId, userId);
            noteDao.updateLikeCount(noteId, -1);
            liked = false;
        } else {
            likeDao.addNoteLike(noteId, userId);
            noteDao.updateLikeCount(noteId, 1);
            liked = true;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        Note fresh = noteDao.findDetailById(noteId);
        result.put("likeCount", fresh.getLikeCount());
        return result;
    }

    // ---------------- 内部方法 ----------------

    private void validateSave(NoteSaveDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new BusinessException(400, "请填写笔记标题");
        }
        if (dto.getCourseId() == null) {
            throw new BusinessException(400, "请选择所属课程");
        }
        if (dto.getSegments() == null || dto.getSegments().isEmpty()) {
            throw new BusinessException(400, "至少添加一个内容片段（文字/图片/音频）");
        }
    }

    private void saveSegments(Long noteId, List<SegmentDTO> segments) {
        int seq = 0;
        for (SegmentDTO seg : segments) {
            String type = seg.getSegType() == null ? "TEXT" : seg.getSegType().toUpperCase();
            if (!List.of("TEXT", "IMAGE", "AUDIO").contains(type)) {
                throw new BusinessException(400, "片段类型非法：" + type);
            }
            segmentDao.insert(noteId, seq++, type, seg.getContentText(), seg.getMediaUrl(), seg.getCaption());
        }
    }

    private void runAiOrganize(Long noteId, String title, List<SegmentDTO> segments) {
        String fullText = segments.stream()
                .map(s -> "TEXT".equalsIgnoreCase(s.getSegType()) ? s.getContentText() : s.getCaption())
                .collect(Collectors.joining("\n"));
        persistAiResult(noteId, title, fullText);
    }

    private void persistAiResult(Long noteId, String title, String fullText) {
        AiResult ai = aiService.organize(title, fullText);
        String keywords = ai.getKeywords() == null ? "" : String.join(",", ai.getKeywords());
        String points = ai.getPoints() == null ? "" : String.join("\n", ai.getPoints());
        noteDao.updateAiResult(noteId, ai.getSummary(), keywords, points, ai.getEngine());
        jdbc.update("INSERT INTO ai_log (note_id, engine, result_text) VALUES (?, ?, ?)",
                noteId, ai.getEngine(), ai.getSummary() + "\n" + keywords + "\n" + points);
    }

    private void assertOwner(Long noteId, Long userId) {
        Long owner = noteDao.findOwnerId(noteId);
        if (owner == null) throw new BusinessException(404, "笔记不存在");
        if (!owner.equals(userId)) throw new BusinessException(403, "只能操作本人笔记");
    }
}

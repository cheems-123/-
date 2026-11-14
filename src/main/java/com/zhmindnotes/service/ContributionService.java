package com.zhmindnotes.service;

import com.zhmindnotes.common.BusinessException;
import com.zhmindnotes.controller.dto.ContributionDTO;
import com.zhmindnotes.dao.ContributionDao;
import com.zhmindnotes.dao.LikeDao;
import com.zhmindnotes.dao.NoteDao;
import com.zhmindnotes.model.Contribution;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 贴吧式共享补充：主帖补充（楼层）+ 楼中楼回复，支持文字/图片/音频。
 */
@Service
public class ContributionService {

    private final ContributionDao contributionDao;
    private final NoteDao noteDao;
    private final LikeDao likeDao;

    public ContributionService(ContributionDao contributionDao, NoteDao noteDao, LikeDao likeDao) {
        this.contributionDao = contributionDao;
        this.noteDao = noteDao;
        this.likeDao = likeDao;
    }

    /** 拉取某篇笔记的补充楼，附带楼中楼回复与点赞状态 */
    public List<Contribution> listTree(Long noteId, Long userId) {
        List<Contribution> tops = contributionDao.listTopByNote(noteId);
        for (Contribution top : tops) {
            top.setReplies(contributionDao.listReplies(top.getId()));
            top.setLiked(userId != null && likeDao.contribLiked(top.getId(), userId));
            if (top.getReplies() != null) {
                top.getReplies().forEach(r ->
                        r.setLiked(userId != null && likeDao.contribLiked(r.getId(), userId)));
            }
        }
        return tops;
    }

    public Long add(Long noteId, Long userId, ContributionDTO dto) {
        if (noteDao.findDetailById(noteId) == null) {
            throw new BusinessException(404, "笔记不存在，无法补充");
        }
        if ((dto.getContent() == null || dto.getContent().isBlank())
                && (dto.getMediaUrl() == null || dto.getMediaUrl().isBlank())) {
            throw new BusinessException(400, "补充内容不能为空（文字或媒体二选一）");
        }
        String mediaType = dto.getMediaType() == null ? "TEXT" : dto.getMediaType().toUpperCase();
        return contributionDao.insert(noteId, userId, dto.getParentId(),
                dto.getContent(), dto.getMediaUrl(), mediaType);
    }

    public Map<String, Object> toggleLike(Long contributionId, Long userId) {
        if (contributionDao.findNoteId(contributionId) == null) {
            throw new BusinessException(404, "补充不存在");
        }
        boolean liked;
        if (likeDao.contribLiked(contributionId, userId)) {
            likeDao.removeContribLike(contributionId, userId);
            contributionDao.incrementLike(contributionId, -1);
            liked = false;
        } else {
            likeDao.addContribLike(contributionId, userId);
            contributionDao.incrementLike(contributionId, 1);
            liked = true;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("contributionId", contributionId);
        return result;
    }
}

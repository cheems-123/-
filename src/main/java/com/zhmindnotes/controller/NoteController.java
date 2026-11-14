package com.zhmindnotes.controller;

import com.zhmindnotes.common.BusinessException;
import com.zhmindnotes.common.Result;
import com.zhmindnotes.common.SessionUtil;
import com.zhmindnotes.controller.dto.NoteSaveDTO;
import com.zhmindnotes.model.Note;
import com.zhmindnotes.service.NoteService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    /** 公开笔记列表：课程分类 + 排序(views/likes/newest) + 关键词 */
    @GetMapping
    public Result<List<Note>> list(@RequestParam(required = false) Long courseId,
                                    @RequestParam(required = false) String sort,
                                    @RequestParam(required = false) String keyword) {
        return Result.ok(noteService.listPublic(courseId, sort, keyword));
    }

    /** 我的笔记（需登录） */
    @GetMapping("/mine")
    public Result<List<Note>> mine(HttpServletRequest request) {
        Long uid = SessionUtil.requireUid(request);
        return Result.ok(noteService.listMine(uid));
    }

    /** 笔记详情：片段、AI 整理结果、点赞状态；阅读量 +1 */
    @GetMapping("/{id}")
    public Result<Note> detail(@PathVariable Long id, HttpServletRequest request) {
        return Result.ok(noteService.openDetail(id, SessionUtil.uid(request)));
    }

    /** 发布笔记（含文字/图片/音频片段），自动触发 AI 整理 */
    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody NoteSaveDTO dto, HttpServletRequest request) {
        Long uid = SessionUtil.requireUid(request);
        Long id = noteService.createNote(uid, dto);
        return Result.ok(Map.of("id", id));
    }

    /** 重新触发 AI 整理（作者本人） */
    @PostMapping("/{id}/organize")
    public Result<Note> organize(@PathVariable Long id, HttpServletRequest request) {
        Long uid = SessionUtil.requireUid(request);
        return Result.ok(noteService.refreshAi(id, uid));
    }

    /** 点赞 / 取消点赞 */
    @PostMapping("/{id}/like")
    public Result<Map<String, Object>> like(@PathVariable Long id, HttpServletRequest request) {
        Long uid = SessionUtil.requireUid(request);
        return Result.ok(noteService.toggleLike(id, uid));
    }

    /** 共享状态切换：0 私有 / 1 公开 */
    @PutMapping("/{id}/share")
    public Result<Void> share(@PathVariable Long id, @RequestBody Map<String, Object> body,
                              HttpServletRequest request) {
        Long uid = SessionUtil.requireUid(request);
        Object status = body.get("shareStatus");
        if (status == null) throw new BusinessException(400, "缺少 shareStatus");
        noteService.changeShare(id, uid, Integer.valueOf(status.toString()));
        return Result.ok();
    }

    /** 删除本人笔记 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long uid = SessionUtil.requireUid(request);
        noteService.deleteNote(id, uid);
        return Result.ok();
    }
}

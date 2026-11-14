package com.zhmindnotes.controller;

import com.zhmindnotes.common.Result;
import com.zhmindnotes.common.SessionUtil;
import com.zhmindnotes.controller.dto.ContributionDTO;
import com.zhmindnotes.model.Contribution;
import com.zhmindnotes.service.ContributionService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 贴吧式共享补充接口。
 */
@RestController
public class ContributionController {

    private final ContributionService contributionService;

    public ContributionController(ContributionService contributionService) {
        this.contributionService = contributionService;
    }

    /** 某篇共享笔记的补充楼（含楼中楼） */
    @GetMapping("/api/notes/{id}/contributions")
    public Result<List<Contribution>> list(@PathVariable Long id, HttpServletRequest request) {
        return Result.ok(contributionService.listTree(id, SessionUtil.uid(request)));
    }

    /** 发表补充（主帖楼层或回复某条补充） */
    @PostMapping("/api/notes/{id}/contributions")
    public Result<Map<String, Object>> add(@PathVariable Long id, @RequestBody ContributionDTO dto,
                                           HttpServletRequest request) {
        Long uid = SessionUtil.requireUid(request);
        Long cid = contributionService.add(id, uid, dto);
        return Result.ok(Map.of("id", cid));
    }

    /** 给补充点赞 / 取消 */
    @PostMapping("/api/contributions/{id}/like")
    public Result<Map<String, Object>> like(@PathVariable Long id, HttpServletRequest request) {
        Long uid = SessionUtil.requireUid(request);
        return Result.ok(contributionService.toggleLike(id, uid));
    }
}

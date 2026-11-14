package com.zhmindnotes.service.ai;

import org.springframework.stereotype.Service;

/**
 * 编排：优先调用预留大模型；未配置或失败时回退规则式整理，保证系统始终可演示。
 */
@Service
public class AiOrganizeServiceImpl implements AiOrganizeService {

    private final LlmAiAdapter llmAdapter;
    private final RuleAiOrganizer ruleOrganizer;

    public AiOrganizeServiceImpl(LlmAiAdapter llmAdapter, RuleAiOrganizer ruleOrganizer) {
        this.llmAdapter = llmAdapter;
        this.ruleOrganizer = ruleOrganizer;
    }

    @Override
    public AiResult organize(String title, String fullText) {
        AiResult result = llmAdapter.tryCall(title, fullText);
        if (result == null) {
            result = ruleOrganizer.organize(title, fullText);
        }
        return result;
    }
}

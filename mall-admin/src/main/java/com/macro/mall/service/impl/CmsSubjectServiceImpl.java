package com.macro.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.macro.mall.common.util.ImageUrlRewriter;
import com.macro.mall.mapper.CmsSubjectMapper;
import com.macro.mall.model.CmsSubject;
import com.macro.mall.model.CmsSubjectExample;
import com.macro.mall.service.CmsSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品专题管理Service实现类 */
@Service
public class CmsSubjectServiceImpl implements CmsSubjectService {
    @Autowired
    private CmsSubjectMapper subjectMapper;

    @Autowired
    private ImageUrlRewriter imageUrlRewriter;

    @Override
    public List<CmsSubject> listAll() {
        List<CmsSubject> subjects = subjectMapper.selectByExample(new CmsSubjectExample());
        if (subjects != null) {
            subjects.forEach(s -> s.setPic(imageUrlRewriter.rewrite(s.getPic())));
        }
        return subjects;
    }

    @Override
    public List<CmsSubject> list(String keyword, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        CmsSubjectExample example = new CmsSubjectExample();
        CmsSubjectExample.Criteria criteria = example.createCriteria();
        if (!StrUtil.isEmpty(keyword)) {
            criteria.andTitleLike("%" + keyword + "%");
        }
        List<CmsSubject> subjects = subjectMapper.selectByExample(example);
        if (subjects != null) {
            subjects.forEach(s -> s.setPic(imageUrlRewriter.rewrite(s.getPic())));
        }
        return subjects;
    }
}

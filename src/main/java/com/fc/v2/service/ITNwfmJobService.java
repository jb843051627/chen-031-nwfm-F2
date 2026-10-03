package com.fc.v2.service;

import java.util.List;

import com.fc.v2.model.auto.TNwfmJob;

/**
 * 器具核验作业 Service 接口（job-lifecycle 形状：批量作业提交/生效/回滚，无增删改查入口）
 *
 * @author fuce
 * @date 2026-10-03
 */
public interface ITNwfmJobService {

    /** 按主键回查作业 */
    TNwfmJob selectTNwfmJobById(Long id);

    /** 按作业号回查作业 */
    TNwfmJob selectByJobNo(String jobNo);

    /** 提交一批作业行：落作业主表 + 逐行明细；同作业号重复提交幂等，返回同一 jobId */
    String submitJob(String jobNo, List<TNwfmJob> rows);

    /** 推进到生效：跨模块写回业务主表 + 流水 + 留痕；部分行失败不回滚整批 */
    int effectJob(String jobNo);

    /** 回滚已生效作业：补偿流水不重复，终态不可再推进 */
    int rollbackJob(String jobNo);
}

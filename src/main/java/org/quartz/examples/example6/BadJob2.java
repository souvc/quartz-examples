/* 
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations 
 * under the License.
 * 
 */
 
package org.quartz.examples.example6;

import java.util.Date;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * 一个演示任务执行异常处理的测试任务类（不可恢复异常版本）
 * </p>
 * 
 * <p>
 * 本任务演示了以下特性：
 * 1. 不可恢复异常的处理机制
 * 2. 异常后取消所有触发器
 * 3. 防止有问题的任务继续执行
 * 4. 有状态任务的异常处理
 * </p>
 * 
 * <p>
 * 关键特性：
 * - @PersistJobDataAfterExecution: 执行后持久化任务数据
 * - @DisallowConcurrentExecution: 禁止并发执行
 * - 演示不可修复的除零异常
 * - 使用 setUnscheduleAllTriggers 停止任务调度
 * </p>
 * 
 * <p>
 * 与 BadJob1 的区别：
 * - BadJob1 修复数据后重新执行
 * - BadJob2 直接取消所有触发器，停止执行
 * </p>
 * 
 * @author Bill Kratzer
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class BadJob2 implements Job {

    // 日志记录器
    private static Logger _log = LoggerFactory.getLogger(BadJob2.class);
    // 计算结果存储变量
    private int calculation;

    /**
     * 空的公共构造函数，用于任务初始化
     * 
     * <p>
     * Quartz 调度器通过反射机制创建任务实例时需要无参构造函数
     * </p>
     */
    public BadJob2() {
    }

    /**
     * <p>
     * 当与此任务关联的 <code>{@link org.quartz.Trigger}</code> 触发时，
     * 由 <code>{@link org.quartz.Scheduler}</code> 调用此方法。
     * </p>
     * 
     * <p>
     * 本方法演示了不可恢复异常的处理机制：
     * 1. 故意触发除零异常
     * 2. 捕获异常并创建 JobExecutionException
     * 3. 设置取消所有触发器标志
     * 4. 抛出异常以停止任务调度
     * </p>
     * 
     * <p>
     * 与 BadJob1 的区别：
     * - BadJob1 会修复数据并重新执行
     * - BadJob2 直接停止所有相关的触发器
     * </p>
     * 
     * @param context 任务执行上下文，包含任务详情和触发器信息
     * @throws JobExecutionException 如果任务执行过程中发生异常
     */
    public void execute(JobExecutionContext context)
        throws JobExecutionException {
        // 获取任务的唯一标识键
        JobKey jobKey = context.getJobDetail().getKey();
        _log.info("---" + jobKey + " 开始执行于 " + new Date());

        // 故意构造的异常示例：
        // 由于除零错误导致的异常
        // 这演示了不可恢复异常的处理方式
        try {
            // 故意设置除数为0，触发除零异常
            int zero = 0;
            calculation = 4815 / zero;
        } catch (Exception e) {
            _log.info("--- 任务执行中发生错误！");
            // 创建 Quartz 专用的任务执行异常
            JobExecutionException e2 = 
                new JobExecutionException(e);
            // Quartz 将自动取消与此任务关联的所有触发器
            // 确保任务不会再次运行
            // 这适用于不可恢复的错误情况
            e2.setUnscheduleAllTriggers(true);
            throw e2;
        }

        _log.info("---" + jobKey + " 执行完成于 " + new Date());
    }

}

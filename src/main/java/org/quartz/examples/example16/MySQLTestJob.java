/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at
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

package org.quartz.examples.example16;

import java.util.Date;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MySQL 测试任务
 *
 * <p>
 * 这是一个简单的任务实现，用于演示 MySQL 集群环境下的任务执行。
 * 该任务会记录执行信息，包括任务标识、执行时间、调度器实例等，
 * 便于观察集群中任务的分布和执行情况。
 * </p>
 *
 * <p>
 * 任务特性：
 * - 支持故障恢复（通过 requestRecovery() 配置）
 * - 记录详细的执行日志
 * - 显示当前执行的调度器实例信息
 * - 模拟简单的业务处理逻辑
 * </p>
 *
 * @author Quartz Examples Team
 */
public class MySQLTestJob implements Job {

    private static Logger _log = LoggerFactory.getLogger(MySQLTestJob.class);

    /**
     * 任务执行方法
     *
     * <p>
     * 当触发器触发时，Quartz 调度器会调用此方法。
     * 在集群环境中，该方法可能在不同的调度器实例上执行。
     * </p>
     *
     * @param context 任务执行上下文，包含任务和触发器的详细信息
     * @throws JobExecutionException 如果任务执行过程中发生错误
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        // 获取任务的关键信息
        JobKey jobKey = context.getJobDetail().getKey();
        String instanceId = null;
        try {
            instanceId = context.getScheduler().getSchedulerInstanceId();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }

        _log.info("===== MySQL 测试任务开始执行 =====");
        _log.info("任务标识: {}", jobKey);
        _log.info("调度器实例: {}", instanceId);
        _log.info("执行时间: {}", new Date());
        _log.info("触发器: {}", context.getTrigger().getKey());
        _log.info("计划执行时间: {}", context.getScheduledFireTime());
        _log.info("实际执行时间: {}", context.getFireTime());

        try {
            // 模拟一些业务处理
            _log.info("正在执行业务逻辑...");

            // 模拟处理时间（1-3秒）
            long processingTime = 1000 + (long)(Math.random() * 2000);
            Thread.sleep(processingTime);

            // 模拟一些数据处理
            int processedRecords = (int)(Math.random() * 100) + 1;
            _log.info("处理了 {} 条记录", processedRecords);

            _log.info("业务逻辑执行完成，耗时: {} 毫秒", processingTime);

        } catch (InterruptedException e) {
            _log.warn("任务执行被中断: {}", e.getMessage());
            // 重新设置中断状态
            Thread.currentThread().interrupt();
            throw new JobExecutionException("任务执行被中断", e);
        } catch (Exception e) {
            _log.error("任务执行过程中发生错误: {}", e.getMessage(), e);
            throw new JobExecutionException("任务执行失败", e);
        }

        _log.info("===== MySQL 测试任务执行完成 =====");
    }
}

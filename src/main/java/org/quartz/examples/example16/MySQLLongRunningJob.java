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
 * MySQL 长时间运行任务
 *
 * <p>
 * 这是一个长时间运行的任务实现，主要用于演示 MySQL 集群环境下的故障转移功能。
 * 当执行此任务的调度器实例发生故障时，其他健康的实例会检测到并接管该任务的执行。
 * </p>
 *
 * <p>
 * 任务特性：
 * - 执行时间较长（10-20秒）
 * - 支持故障恢复（通过 requestRecovery() 配置）
 * - 定期输出进度信息
 * - 可以被中断和恢复
 * </p>
 *
 * <p>
 * 故障转移测试方法：
 * 1. 启动多个调度器实例
 * 2. 观察长时间任务在某个实例上开始执行
 * 3. 在任务执行过程中关闭该实例
 * 4. 观察其他实例是否接管并重新执行该任务
 * </p>
 *
 * <p>
 * 注意：由于任务执行时间较长，建议在测试环境中使用，
 * 生产环境中应根据实际业务需求调整执行时间。
 * </p>
 *
 * @author Quartz Examples Team
 */
public class MySQLLongRunningJob implements Job {

    private static Logger _log = LoggerFactory.getLogger(MySQLLongRunningJob.class);

    /**
     * 长时间运行任务执行方法
     *
     * <p>
     * 该方法会执行一个较长时间的模拟任务，期间会定期输出进度信息。
     * 如果在执行过程中调度器实例发生故障，该任务会被其他实例接管并重新执行。
     * </p>
     *
     * @param context 任务执行上下文
     * @throws JobExecutionException 如果任务执行过程中发生错误
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {

        JobKey jobKey = context.getJobDetail().getKey();
        String instanceId = null;
        try {
            instanceId = context.getScheduler().getSchedulerInstanceId();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }

        _log.info("===== MySQL 长时间运行任务开始执行 =====");
        _log.info("任务标识: {}", jobKey);
        _log.info("调度器实例: {}", instanceId);
        _log.info("执行时间: {}", new Date());
        _log.info("预计执行时长: 10-20 秒");
        _log.info("提示: 您可以在任务执行过程中关闭此实例来测试故障转移功能");

        try {
            // 模拟长时间运行的任务（10-20秒）
            int totalSteps = 10 + (int)(Math.random() * 10); // 10-20步
            int stepDuration = 1000; // 每步1秒

            _log.info("开始执行长时间任务，总共 {} 步，每步约 {} 毫秒", totalSteps, stepDuration);

            for (int step = 1; step <= totalSteps; step++) {
                // 检查线程是否被中断
                if (Thread.currentThread().isInterrupted()) {
                    _log.warn("任务在第 {} 步被中断", step);
                    throw new InterruptedException("任务执行被中断");
                }

                // 模拟每一步的处理
                _log.info("正在执行第 {}/{} 步...", step, totalSteps);

                // 模拟处理时间
                Thread.sleep(stepDuration);

                // 模拟一些业务逻辑
                int processedItems = (int)(Math.random() * 50) + 1;
                _log.info("第 {} 步完成，处理了 {} 个项目", step, processedItems);

                // 每5步输出一次进度摘要
                if (step % 5 == 0) {
                    double progress = (double) step / totalSteps * 100;
                    _log.info("===== 进度报告: {:.1f}% 完成 ({}/{}) =====");
                    _log.info("当前执行实例: {}", instanceId);
                    _log.info("剩余步骤: {}", totalSteps - step);
                }
            }

            _log.info("长时间任务执行完成！");
            _log.info("总执行时间: 约 {} 秒", totalSteps);

        } catch (InterruptedException e) {
            _log.warn("长时间任务执行被中断: {}", e.getMessage());
            _log.info("如果这是由于实例故障导致的，其他实例应该会接管此任务");
            // 重新设置中断状态
            Thread.currentThread().interrupt();
            throw new JobExecutionException("长时间任务执行被中断", e);
        } catch (Exception e) {
            _log.error("长时间任务执行过程中发生错误: {}", e.getMessage(), e);
            throw new JobExecutionException("长时间任务执行失败", e);
        }

        _log.info("===== MySQL 长时间运行任务执行完成 =====");
        _log.info("任务成功完成，没有发生故障转移");
    }
}

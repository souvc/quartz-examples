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
 
package org.quartz.examples.example3;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

/**
 * 简单任务类
 * 
 * <p>
 * 这是一个简单的任务实现类，用于演示 Cron 触发器的执行效果。
 * 该任务会被不同的 Cron 触发器多次调度执行，每次执行时会打印任务信息和执行时间。
 * </p>
 * 
 * <p>
 * 主要功能:
 * - 实现 Quartz Job 接口
 * - 打印任务执行的详细信息
 * - 记录任务执行的时间戳
 * - 展示任务的身份标识信息
 * </p>
 * 
 * @author Bill Kratzer
 */
public class SimpleJob implements Job {

    // 日志记录器，用于输出任务执行信息
    // 使用 SLF4J 日志框架，便于日志管理和配置
    private static Logger _log = LoggerFactory.getLogger(SimpleJob.class);

    /**
     * 无参构造函数
     * 
     * Quartz 要求任务类必须有一个公共的无参构造函数，
     * 这样调度器就可以在需要时实例化该类。
     * 这是 Quartz 框架的基本要求。
     */
    public SimpleJob() {
    }

    /**
     * 任务执行方法
     * 
     * <p>
     * 当与此任务关联的 {@link org.quartz.Trigger} 触发时，
     * 由 {@link org.quartz.Scheduler} 调用此方法。
     * </p>
     * 
     * <p>
     * 该方法包含任务的具体执行逻辑，在本示例中主要是:
     * - 获取任务的身份标识信息
     * - 记录任务执行的时间
     * - 输出任务执行日志
     * </p>
     * 
     * @param context 任务执行上下文，包含任务和触发器的详细信息
     * @throws JobExecutionException 如果任务执行过程中发生异常
     */
    public void execute(JobExecutionContext context)
        throws JobExecutionException {

        // 获取任务的唯一标识键，包含任务名称和组名
        // JobKey 用于在调度器中唯一标识一个任务
        JobKey jobKey = context.getJobDetail().getKey();
        
        // 记录任务执行信息，包括任务标识和执行时间
        // 这有助于监控和调试 Cron 触发器的执行情况
        _log.info("SimpleJob 执行: " + jobKey + " 执行时间: " + new Date());
    }

}

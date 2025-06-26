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
 
package org.quartz.examples.example2;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

/**
 * <p>
 * 这是一个简单的任务类，用于演示简单触发器的执行效果。
 * 该任务会被多个触发器调度执行，展示不同的调度策略。
 * </p>
 * 
 * @author Bill Kratzer
 */
public class SimpleJob implements Job {

    private static Logger _log = LoggerFactory.getLogger(SimpleJob.class);

    /**
     * 空构造函数，用于任务初始化
     * Quartz 要求 Job 实现类必须有一个无参构造函数
     */
    public SimpleJob() {
    }

    /**
     * <p>
     * 当与此任务关联的 <code>{@link org.quartz.Trigger}</code> 触发时，
     * 由 <code>{@link org.quartz.Scheduler}</code> 调用此方法。
     * </p>
     * 
     * @param context 任务执行上下文，包含任务和触发器的详细信息
     * @throws JobExecutionException 如果任务执行过程中发生异常
     */
    public void execute(JobExecutionContext context)
        throws JobExecutionException {

        // 这个任务简单地打印出任务名称和执行时间
        // 通过任务执行上下文获取任务的详细信息
        JobKey jobKey = context.getJobDetail().getKey();
        
        // 记录任务执行信息，包括任务键和当前执行时间
        _log.info("SimpleJob 说: " + jobKey + " 正在执行，时间: " + new Date());
    }

}

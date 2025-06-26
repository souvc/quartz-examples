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
 
package org.quartz.examples.example8;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

/**
 * 简单任务实现类
 * 
 * 这是一个简单的任务实现，用于演示 Quartz 日历功能。
 * 该任务会在每次执行时输出任务标识和执行时间，
 * 帮助观察日历排除对任务调度的影响。
 * 
 * 功能特点：
 * - 实现 Job 接口，提供基本的任务执行能力
 * - 输出详细的执行信息，包括任务键和执行时间
 * - 适用于各种调度场景的测试和演示
 * 
 * 在日历示例中的作用：
 * - 作为被调度的目标任务
 * - 通过执行日志验证日历排除功能
 * - 展示任务在排除日期时的推迟执行效果
 * 
 * @author Bill Kratzer
 */
public class SimpleJob implements Job {

    // 日志记录器，用于输出任务执行信息
    private static Logger _log = LoggerFactory.getLogger(SimpleJob.class);

    /**
     * 空的公共构造函数
     * 
     * Quartz 调度器通过反射机制创建任务实例时需要无参构造函数。
     * 该构造函数为任务实例化提供支持。
     */
    public SimpleJob() {
    }

    /**
     * 任务执行方法
     * 
     * 当与此任务关联的 {@link org.quartz.Trigger} 触发时，
     * 由 {@link org.quartz.Scheduler} 调用此方法执行任务逻辑。
     * 
     * 在日历示例中，此方法的作用：
     * 1. 输出任务的标识信息和执行时间
     * 2. 帮助验证日历排除功能是否正常工作
     * 3. 通过执行时间观察任务是否在排除日期被正确推迟
     * 
     * @param context 任务执行上下文，包含任务和触发器的详细信息
     * @throws JobExecutionException 如果任务执行过程中发生异常
     */
    public void execute(JobExecutionContext context)
        throws JobExecutionException {

        // 获取任务的唯一标识（包含任务名称和组名）
        JobKey jobKey = context.getJobDetail().getKey();
        
        // 输出任务执行信息：任务标识和当前执行时间
        // 这有助于观察日历排除对任务调度时间的影响
        _log.info("SimpleJob 说: " + jobKey + " 正在执行，执行时间: " + new Date());
    }

}

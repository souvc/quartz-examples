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
 
package org.quartz.examples.example12;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

/**
 * 简单任务实现类 - RMI 远程调度演示
 * 
 * 本类是一个简单的 Job 实现，专门用于 RMI 远程调度演示。
 * 当远程客户端通过 RMI 调度此任务时，它会在服务器端执行并输出相关信息。
 * 
 * 主要功能：
 * - 输出任务执行信息（任务名称、执行时间）
 * - 显示从任务数据映射中获取的消息
 * - 演示远程任务的实际执行过程
 * - 提供简单的日志输出用于验证远程调度是否成功
 * 
 * @author James House
 */
public class SimpleJob implements Job {

    // 任务数据映射中消息参数的键名
    public static final String MESSAGE = "msg";

    // 日志记录器，用于输出任务执行信息
    private static Logger _log = LoggerFactory.getLogger(SimpleJob.class);

    /**
     * 空构造函数
     * 
     * Quartz 要求任务类必须有一个公共的空构造函数，
     * 以便调度器可以在需要时实例化该类。
     */
    public SimpleJob() {
    }

    /**
     * 执行任务的主要方法
     * 
     * 当与此任务关联的触发器被触发时，Quartz 调度器会调用此方法。
     * 在 RMI 远程调度演示中，此方法会在服务器端执行，
     * 输出任务的基本信息和从客户端传递的消息。
     * 
     * @param context 任务执行上下文，包含任务详情、触发器信息和数据映射
     * @throws JobExecutionException 如果任务执行过程中发生异常
     */
    public void execute(JobExecutionContext context)
        throws JobExecutionException {

        // 获取任务的键（包含任务名称和分组信息）
        JobKey jobKey = context.getJobDetail().getKey();

        // 从任务数据映射中获取客户端传递的消息
        // 这个消息是在客户端调度任务时设置的
        String message = (String) context.getJobDetail().getJobDataMap().get(MESSAGE);

        // 输出任务执行信息，包括任务标识和执行时间
        _log.info("SimpleJob: " + jobKey + " 正在执行，时间: " + new Date());
        // 输出从客户端传递的消息，验证远程数据传输
        _log.info("SimpleJob: 消息: " + message);
    }

    

}

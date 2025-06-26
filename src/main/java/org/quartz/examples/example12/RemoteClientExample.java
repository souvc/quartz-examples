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

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RMI 远程调度客户端示例
 * 
 * 本类演示如何创建一个客户端程序，通过 RMI 远程连接到 Quartz 调度服务器
 * 来调度任务。客户端不需要运行自己的调度器，而是连接到远程服务器的调度器。
 * 
 * 主要功能：
 * - 通过 RMI 连接到远程 Quartz 调度器
 * - 创建任务和触发器
 * - 将任务提交到远程调度器执行
 * - 演示分布式任务调度的基本用法
 * 
 * 注意：客户端使用 client.properties 配置文件来指定远程服务器的地址和端口。
 * 
 * @author James House, Bill Kratzer
 */
public class RemoteClientExample {

    /**
     * 运行远程客户端示例
     * 
     * 此方法连接到远程 Quartz 调度器，创建一个简单的任务和触发器，
     * 然后将其提交到远程服务器执行。
     * 
     * @throws Exception 如果连接远程调度器或调度任务时发生错误
     */
    public void run() throws Exception {

        Logger log = LoggerFactory.getLogger(RemoteClientExample.class);

        // 首先获取调度器的引用
        // StdSchedulerFactory 会读取 client.properties 配置文件
        // 由于配置了 rmi.proxy=true，这将创建一个远程调度器代理
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler sched = sf.getScheduler();

        // 定义任务并请求运行
        // 创建一个 SimpleJob 类型的任务，任务名为 "remotelyAddedJob"
        JobDetail job = newJob(SimpleJob.class)
            .withIdentity("remotelyAddedJob", "default")
            .build();
        
        // 向任务数据映射中添加消息参数
        // 这个消息将在任务执行时被 SimpleJob 读取并输出
        JobDataMap map = job.getJobDataMap();
        map.put("msg", "您的远程添加任务已执行！");
        
        // 创建触发器，使用 Cron 表达式定义执行时间
        // "/5 * * ? * *" 表示每 5 秒执行一次
        Trigger trigger = newTrigger()
            .withIdentity("remotelyAddedTrigger", "default")
            .forJob(job.getKey())
            .withSchedule(cronSchedule("/5 * * ? * *"))
            .build();

        // 将任务调度到远程服务器
        // 这个调用会通过 RMI 将任务和触发器发送到远程调度器
        sched.scheduleJob(job, trigger);

        log.info("远程任务已调度完成。");
    }

    public static void main(String[] args) throws Exception {

        RemoteClientExample example = new RemoteClientExample();
        example.run();
    }

}

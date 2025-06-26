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

package org.quartz.examples.example1;

import static org.quartz.DateBuilder.evenMinuteDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 这个示例演示了如何启动和关闭Quartz调度器，以及如何在Quartz中调度一个任务。
 *
 * scheduler实例化后，可以启动(start)、暂停(stand-by)、停止(shutdown)。
 * 注意：scheduler被停止后，除非重新实例化，否则不能重新启动；
 * 只有当scheduler启动后，即使处于暂停状态也不行，trigger才会被触发（job才会被执行）。
 *
 * @author Bill Kratzer
 */
public class SimpleExample {

  public void run() throws Exception {
    // 获取日志记录器
    Logger log = LoggerFactory.getLogger(SimpleExample.class);

    log.info("------- 初始化中 ----------------------");

    // 首先我们必须获取一个调度器的引用
    SchedulerFactory sf = new StdSchedulerFactory();
    Scheduler sched = sf.getScheduler();

    log.info("------- 初始化完成 -----------");

    // 计算下一个整分钟的时间
    Date runTime = evenMinuteDate(new Date());
    //记录时间
    log.info("任务将在: {} 运行", runTime);


    log.info("------- 调度任务中  -------------------");

    // 定义任务并将其绑定到我们的HelloJob类
    JobDetail job = newJob(HelloJob.class).withIdentity("job1", "group1").build();

    // 创建触发器，在下一个整分钟时触发任务
    Trigger trigger = newTrigger().withIdentity("trigger1", "group1").startAt(runTime).build();

    // 告诉quartz使用我们的触发器来调度任务
    sched.scheduleJob(job, trigger);
    log.info("{} 将在以下时间运行: {}", job.getKey(), runTime);

    // 启动调度器（在调度器启动之前，实际上什么都不会运行）
    sched.start();

    log.info("------- 调度器已启动 -----------------");

    // 等待足够长的时间，让调度器有机会运行任务！
    log.info("------- 等待65秒... -------------");
    try {
      // 等待65秒来展示任务执行
      Thread.sleep(65L * 1000L);
      // 执行中...
    } catch (Exception e) {
      // 忽略异常
    }

    // 关闭调度器
    log.info("------- 正在关闭 ---------------------");
    sched.shutdown(true);
    log.info("------- 关闭完成 -----------------");
  }

  public static void main(String[] args) throws Exception {
    // 创建示例实例并运行
    SimpleExample example = new SimpleExample();
    example.run();
  }

}

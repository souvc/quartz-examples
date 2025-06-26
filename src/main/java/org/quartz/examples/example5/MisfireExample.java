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
 
package org.quartz.examples.example5;

import static org.quartz.DateBuilder.nextGivenSecondDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerMetaData;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 失火机制演示示例
 * 
 * <p>
 * 本示例演示了 Quartz 调度器中任务失火（Misfire）机制的工作原理。
 * 通过创建执行时间超过触发间隔的任务，观察不同失火处理策略的效果。
 * </p>
 * 
 * <p>
 * 示例特点：
 * - 创建两个相同的任务，使用不同的失火处理策略
 * - 任务执行时间（10秒）超过触发间隔（3秒）
 * - 对比默认策略和立即重新调度策略的行为差异
 * - 使用有状态任务防止并发执行
 * </p>
 * 
 * @author <a href="mailto:bonhamcm@thirdeyeconsulting.com">Chris Bonham</a>
 */
public class MisfireExample {

  public void run() throws Exception {
    // 初始化日志记录器
    Logger log = LoggerFactory.getLogger(MisfireExample.class);

    log.info("------- 初始化调度器 -------------------");

    // 首先获取调度器工厂的引用，然后创建调度器实例
    SchedulerFactory sf = new StdSchedulerFactory();
    Scheduler sched = sf.getScheduler();

    log.info("------- 调度器初始化完成 --------");

    log.info("------- 开始调度任务 ----------------");

    // 任务可以在调度器启动之前进行调度

    // 获取一个"整齐的"未来时间点（下一个15秒的倍数时刻）
    Date startTime = nextGivenSecondDate(null, 15);

    // 创建第一个有状态任务，每3秒执行一次
    // 但任务本身会延迟10秒执行，这会导致失火
    JobDetail job = newJob(StatefulDumbJob.class).withIdentity("statefulJob1", "group1")
        .usingJobData(StatefulDumbJob.EXECUTION_DELAY, 10000L).build();

    // 创建第一个触发器，使用默认的失火处理策略（智能策略）
    SimpleTrigger trigger = newTrigger().withIdentity("trigger1", "group1").startAt(startTime)
        .withSchedule(simpleSchedule().withIntervalInSeconds(3).repeatForever()).build();

    // 将第一个任务和触发器提交给调度器
    Date ft = sched.scheduleJob(job, trigger);
    log.info(job.getKey() + " 将在以下时间运行: " + ft + " 重复次数: " + trigger.getRepeatCount() + " 次，每 "
             + trigger.getRepeatInterval() / 1000 + " 秒执行一次");

    // 创建第二个有状态任务，同样每3秒执行一次
    // 但任务本身会延迟10秒执行，这会导致失火
    job = newJob(StatefulDumbJob.class).withIdentity("statefulJob2", "group1")
        .usingJobData(StatefulDumbJob.EXECUTION_DELAY, 10000L).build();  // 设置执行延迟为10秒

    // 创建第二个触发器，使用立即重新调度的失火处理策略
    trigger = newTrigger()
        .withIdentity("trigger2", "group1")
        .startAt(startTime)
        .withSchedule(simpleSchedule().withIntervalInSeconds(3).repeatForever()  // 每3秒触发一次，无限重复
                          .withMisfireHandlingInstructionNowWithExistingCount()) // 失火时立即重新调度
        .build();

    // 将第二个任务和触发器提交给调度器
    ft = sched.scheduleJob(job, trigger);
    log.info(job.getKey() + " 将在以下时间运行: " + ft + " 重复次数: " + trigger.getRepeatCount() + " 次，每 "
             + trigger.getRepeatInterval() / 1000 + " 秒执行一次");

    log.info("------- 启动调度器 ----------------");

    // 所有任务都已添加到调度器中，但在调度器启动之前
    // 任何任务都不会运行
    sched.start();

    log.info("------- 调度器已启动 -----------------");

    // 等待10分钟以观察任务执行情况和失火行为
    log.info("------- 等待10分钟观察失火行为... -------------");
    try {
      // 等待10分钟（600秒）来观察任务的执行和失火处理
      Thread.sleep(600L * 1000L);
    } catch (Exception e) {
      // 忽略中断异常
    }

    log.info("------- 开始关闭调度器 ---------------------");

    // 关闭调度器，参数true表示等待当前正在执行的任务完成
    sched.shutdown(true);

    log.info("------- 调度器关闭完成 -----------------");

    // 获取调度器的元数据信息
        SchedulerMetaData metaData = sched.getMetaData();
        log.info("总共执行了 " + metaData.getNumberOfJobsExecuted() + " 个任务。");

    }

    /**
     * 主方法 - 程序入口点
     * 
     * <p>
     * 创建 MisfireExample 实例并运行示例，演示失火机制的工作原理。
     * 运行此示例时，你会观察到两个触发器具有相同的调度计划，触发相同的任务。
     * 触发器"希望"每3秒触发一次，但任务需要10秒才能执行完成。
     * 因此，当任务完成执行时，触发器已经"失火"了。
     * </p>
     * 
     * <p>
     * 你会看到一个任务使用立即重新调度的失火指令，这会导致它在检测到失火时立即触发。
     * 另一个触发器使用默认的"智能策略"失火指令，这会使触发器前进到下一个触发时间
     * （跳过那些已错过的时间），因此它不会立即重新触发，而是在下一个预定时间触发。
     * </p>
     * 
     * @param args 命令行参数（未使用）
     * @throws Exception 如果示例执行过程中发生异常
     */
    public static void main(String[] args) throws Exception {

        MisfireExample example = new MisfireExample();
        example.run();

    }

}

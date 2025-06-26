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
 
package org.quartz.examples.example7;

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
 * <p>
 * 任务中断机制演示程序，展示了Quartz调度器如何中断正在运行的任务。
 * </p>
 * <p>
 * 本示例演示了以下核心功能：
 * <ul>
 * <li><strong>可中断任务调度</strong>：创建和调度实现InterruptableJob接口的任务</li>
 * <li><strong>定期中断测试</strong>：每7秒发送一次中断信号给正在运行的任务</li>
 * <li><strong>中断响应观察</strong>：观察任务如何响应中断信号并优雅退出</li>
 * <li><strong>任务重新调度</strong>：被中断的任务会自动重新调度执行</li>
 * <li><strong>执行统计监控</strong>：显示调度器的执行统计信息</li>
 * </ul>
 * </p>
 * <p>
 * 执行流程说明：
 * <ol>
 * <li>创建一个每5秒执行一次的可中断任务</li>
 * <li>任务每次执行需要4秒时间（模拟长时间运行）</li>
 * <li>主程序每7秒发送一次中断信号</li>
 * <li>观察任务的启动、中断、重启循环过程</li>
 * <li>运行50次中断测试后关闭调度器</li>
 * </ol>
 * </p>
 * <p>
 * 关键学习点：
 * <ul>
 * <li>InterruptableJob接口的使用方法</li>
 * <li>scheduler.interrupt()方法的调用</li>
 * <li>任务中断标志的检查机制</li>
 * <li>中断后的优雅退出处理</li>
 * <li>被中断任务的自动重新调度</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:bonhamcm@thirdeyeconsulting.com">Chris Bonham</a>
 */
public class InterruptExample {

  public void run() throws Exception {
    // 初始化日志记录器，用于输出示例执行过程的详细信息
    final Logger log = LoggerFactory.getLogger(InterruptExample.class);

    log.info("------- 开始初始化调度器 ----------------------");

    // 首先获取调度器实例
    // 使用标准调度器工厂创建默认配置的调度器
    SchedulerFactory sf = new StdSchedulerFactory();
    Scheduler sched = sf.getScheduler();

    log.info("------- 调度器初始化完成 -----------");

    log.info("------- 开始调度任务 -------------------");

    // 获取一个"整齐"的未来时间点（15秒后的整秒时刻）
    // 这样可以让任务在一个容易观察的时间点开始执行
    Date startTime = nextGivenSecondDate(null, 15);

    // 创建可中断任务实例
    // 使用DumbInterruptableJob类，它实现了InterruptableJob接口
    JobDetail job = newJob(DumbInterruptableJob.class).withIdentity("interruptableJob1", "group1").build();

    // 创建简单触发器：每5秒执行一次，无限重复
    // 任务执行需要4秒，触发间隔是5秒，所以任务之间有1秒的间隔
    SimpleTrigger trigger = newTrigger().withIdentity("trigger1", "group1").startAt(startTime)
        .withSchedule(simpleSchedule().withIntervalInSeconds(5).repeatForever()).build();

    // 将任务和触发器提交给调度器
    Date ft = sched.scheduleJob(job, trigger);
    log.info(job.getKey() + " 将在以下时间运行: " + ft + " 并重复: " + trigger.getRepeatCount() + " 次, 每 "
             + trigger.getRepeatInterval() / 1000 + " 秒执行一次");

    // 启动调度器（任务只有在调度器启动后才会开始触发）
    // 这是一个重要步骤：调度器必须显式启动才能执行任务
    sched.start();
    log.info("------- 调度器已启动 -----------------");

    log.info("------- 开始循环测试：每7秒中断一次任务 ----------");
    // 执行50次中断测试，观察任务的中断和重启行为
    // 每次循环：等待7秒 -> 发送中断信号 -> 观察任务响应
    for (int i = 0; i < 50; i++) {
      try {
        // 等待7秒，让任务有时间执行
        // 由于任务执行需要4秒，7秒的间隔确保任务能够完成或被中断
        Thread.sleep(7000L);
        
        // 告诉调度器中断指定的任务
        // 这会调用DumbInterruptableJob的interrupt()方法
        sched.interrupt(job.getKey());
        
        log.info("第 " + (i + 1) + " 次中断信号已发送");
      } catch (Exception e) {
        // 忽略中断过程中的异常，继续测试
        log.warn("中断过程中发生异常: " + e.getMessage());
      }
    }

    log.info("------- 开始关闭调度器 ---------------------");

    // 关闭调度器，参数true表示等待当前正在执行的任务完成
    // 这确保了优雅关闭，不会强制中断正在运行的任务
    sched.shutdown(true);

    log.info("------- 调度器关闭完成 -----------------");
    
    // 获取并输出调度器的执行统计信息
    // 这些信息有助于了解示例运行期间的任务执行情况
    SchedulerMetaData metaData = sched.getMetaData();
    log.info("总共执行了 " + metaData.getNumberOfJobsExecuted() + " 个任务实例。");
    log.info("调度器运行时间: " + (metaData.getRunningSince() != null ? 
             "从 " + metaData.getRunningSince() + " 开始" : "未知"));

  }

  /**
   * <p>
   * 程序入口方法，演示Quartz中的任务中断机制。
   * </p>
   * <p>
   * 本示例展示了如何：
   * <ul>
   * <li>创建和调度可中断的任务</li>
   * <li>定期发送中断信号给正在运行的任务</li>
   * <li>观察任务如何响应中断并优雅退出</li>
   * <li>监控被中断任务的自动重新调度</li>
   * </ul>
   * </p>
   * <p>
   * 执行过程中你将看到：
   * <ol>
   * <li>任务每5秒启动一次</li>
   * <li>每个任务执行4秒（模拟工作）</li>
   * <li>主程序每7秒发送中断信号</li>
   * <li>任务收到中断后立即退出</li>
   * <li>调度器自动重新调度被中断的任务</li>
   * </ol>
   * </p>
   * 
   * @param args 命令行参数（本示例中未使用）
   * @throws Exception 如果示例执行过程中发生异常
   */
  public static void main(String[] args) throws Exception {

    // 创建示例实例并运行
    InterruptExample example = new InterruptExample();
    example.run();
  }

}

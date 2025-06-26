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
 
package org.quartz.examples.example9;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Matcher;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerMetaData;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.KeyMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务监听器功能演示类
 * 
 * <p>本示例演示了 {@code JobListener} 的行为和使用方法。具体展示了如何使用任务监听器
 * 在一个任务成功执行完成后自动触发另一个任务的执行。</p>
 * 
 * <p>功能特性：</p>
 * <ul>
 * <li>任务监听器注册：将监听器绑定到特定任务</li>
 * <li>事件驱动调度：基于任务完成事件触发新任务</li>
 * <li>动态任务创建：在运行时动态创建和调度任务</li>
 * <li>任务链式执行：实现任务间的依赖关系</li>
 * </ul>
 * 
 * <p>执行流程：</p>
 * <ol>
 * <li>创建并调度 SimpleJob1</li>
 * <li>注册 Job1Listener 监听 SimpleJob1</li>
 * <li>SimpleJob1 执行完成后触发监听器</li>
 * <li>监听器自动创建并调度 SimpleJob2</li>
 * <li>观察任务的链式执行效果</li>
 * </ol>
 * 
 * <p>学习要点：</p>
 * <ul>
 * <li>JobListener 接口的实现和使用</li>
 * <li>监听器的注册和绑定机制</li>
 * <li>在监听器中动态调度新任务</li>
 * <li>任务间依赖关系的实现方式</li>
 * </ul>
 */
public class ListenerExample {

  public void run() throws Exception {
    Logger log = LoggerFactory.getLogger(ListenerExample.class);

    log.info("------- 初始化调度器 ----------------------");

    // 首先获取调度器实例
    // 使用标准调度器工厂创建调度器
    SchedulerFactory sf = new StdSchedulerFactory();
    Scheduler sched = sf.getScheduler();

    log.info("------- 调度器初始化完成 ---------------------");

    log.info("------- 调度任务配置 -----------------------");

    // 创建一个立即执行的任务
    // 使用 SimpleJob1 类创建任务详情，设置任务标识为 "job1"
    JobDetail job = newJob(SimpleJob1.class).withIdentity("job1").build();

    // 创建触发器，设置立即启动
    Trigger trigger = newTrigger().withIdentity("trigger1").startNow().build();

    // 设置任务监听器
    // 创建 Job1Listener 实例，用于监听 job1 的执行事件
    JobListener listener = new Job1Listener();
    // 创建匹配器，指定监听器只监听 job1 这个特定任务
    Matcher<JobKey> matcher = KeyMatcher.keyEquals(job.getKey());
    // 将监听器注册到调度器，并指定监听范围
    sched.getListenerManager().addJobListener(listener, matcher);

    // 将任务和触发器提交给调度器
    sched.scheduleJob(job, trigger);

    // 所有任务都已添加到调度器，但只有启动调度器后任务才会执行
    log.info("------- 启动调度器 -------------------------");
    sched.start();

    // 等待 30 秒观察任务执行情况
    // 注意：在这期间会看到 SimpleJob1 执行，然后监听器触发 SimpleJob2 执行
    log.info("------- 等待 30 秒观察任务执行... --------------");
    try {
      // 等待 30 秒以便观察任务的链式执行效果
      Thread.sleep(30L * 1000L);
      // 在此期间任务会按照监听器的逻辑依次执行
    } catch (Exception e) {
      // 忽略中断异常
    }

    // 关闭调度器
    log.info("------- 关闭调度器 -------------------------");
    // 参数 true 表示等待当前正在执行的任务完成后再关闭
    sched.shutdown(true);
    log.info("------- 调度器关闭完成 ---------------------");

    // 获取并输出调度器执行统计信息
    SchedulerMetaData metaData = sched.getMetaData();
    log.info("总共执行了 " + metaData.getNumberOfJobsExecuted() + " 个任务。");

  }

  /**
   * 程序入口方法
   * 
   * <p>演示 Quartz 任务监听器功能的完整流程：</p>
   * <ul>
   * <li>创建和注册任务监听器</li>
   * <li>监听器与特定任务的绑定</li>
   * <li>基于事件驱动的任务链式执行</li>
   * <li>动态任务创建和调度</li>
   * </ul>
   * 
   * <p>运行此示例可以学习到：</p>
   * <ul>
   * <li>JobListener 接口的实现和使用方法</li>
   * <li>如何在任务完成后自动触发其他任务</li>
   * <li>监听器的注册和匹配机制</li>
   * <li>事件驱动的任务调度模式</li>
   * </ul>
   * 
   * @param args 命令行参数（本示例中未使用）
   * @throws Exception 如果示例执行过程中发生异常
   */
  public static void main(String[] args) throws Exception {

    ListenerExample example = new ListenerExample();
    example.run();
  }

}

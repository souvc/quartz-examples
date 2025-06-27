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

import static org.quartz.DateBuilder.futureDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MySQL 集群调度演示程序
 * 
 * <p>
 * 本示例演示如何使用 MySQL 数据库作为 JobStore 来实现 Quartz 集群功能。
 * 该示例创建多个任务，展示集群环境下的任务分布、故障转移和负载均衡。
 * </p>
 * 
 * <p>
 * 运行前准备：
 * 1. 安装并启动 MySQL 数据库
 * 2. 创建 quartz 数据库
 * 3. 执行 Quartz 提供的 MySQL 建表脚本
 * 4. 配置数据库连接信息
 * </p>
 * 
 * <p>
 * 集群配置要求：
 * - 所有实例都应使用相同的 "org.quartz.scheduler.instanceName"
 * - 集群中的每个实例必须使用不同的 "org.quartz.scheduler.instanceId"
 * - 所有实例必须连接到同一个 MySQL 数据库
 * - 确保系统时间在所有集群节点间同步
 * </p>
 * 
 * <p>
 * 支持的命令行参数：
 * - "clearJobs" - 清除数据库中所有现有的任务和触发器
 * - "dontScheduleJobs" - 不调度新任务，仅用于观察现有任务的恢复
 * </p>
 * 
 * <p>
 * 集群特性演示：
 * - 任务在多个实例间自动分布执行
 * - 当某个实例故障时，其任务会被其他实例接管
 * - 支持任务的故障恢复和状态持久化
 * - 实现真正的高可用性任务调度
 * </p>
 * 
 * @author Quartz Examples Team
 */
public class MySQLClusterExample {

  private static Logger _log = LoggerFactory.getLogger(MySQLClusterExample.class);

  /**
   * 运行集群示例
   * 
   * @param inClearJobs 是否清除现有任务
   * @param inScheduleJobs 是否调度新任务
   */
  public void run(boolean inClearJobs, boolean inScheduleJobs) throws Exception {

    // 首先我们必须获取对调度器的引用
    SchedulerFactory sf = new StdSchedulerFactory();
    Scheduler sched = sf.getScheduler();

    if (inClearJobs) {
      _log.warn("***** 删除现有的任务/触发器 *****");
      sched.clear();
    }

    if (inScheduleJobs) {

      // 创建任务和触发器
      JobDetail job1 = newJob(MySQLTestJob.class)
          .withIdentity("MySQLTestJob1", "group_1")
          .requestRecovery() // 请求恢复 - 如果实例故障，任务会被其他实例接管
          .build();

      SimpleTrigger trigger1 = newTrigger()
          .withIdentity("trigger_1", "group_1")
          .startAt(futureDate(1, IntervalUnit.SECOND))
          .withSchedule(simpleSchedule()
              .withIntervalInSeconds(10)
              .repeatForever())
          .build();

      // 告诉 Quartz 使用我们的触发器来调度任务
      sched.scheduleJob(job1, trigger1);
      _log.info("任务1已调度 - 任务标识: " + job1.getKey() + ", 下次运行时间: " + trigger1.getNextFireTime());

      // 创建第二个任务
      JobDetail job2 = newJob(MySQLStatefulJob.class)
          .withIdentity("MySQLStatefulJob2", "group_1")
          .requestRecovery()
          .build();

      SimpleTrigger trigger2 = newTrigger()
          .withIdentity("trigger_2", "group_1")
          .startAt(futureDate(2, IntervalUnit.SECOND))
          .withSchedule(simpleSchedule()
              .withIntervalInSeconds(15)
              .repeatForever())
          .build();

      sched.scheduleJob(job2, trigger2);
      _log.info("任务2已调度 - 任务标识: " + job2.getKey() + ", 下次运行时间: " + trigger2.getNextFireTime());

      // 创建第三个任务 - 长时间运行的任务
      JobDetail job3 = newJob(MySQLLongRunningJob.class)
          .withIdentity("MySQLLongRunningJob3", "group_2")
          .requestRecovery()
          .build();

      SimpleTrigger trigger3 = newTrigger()
          .withIdentity("trigger_3", "group_2")
          .startAt(futureDate(3, IntervalUnit.SECOND))
          .withSchedule(simpleSchedule()
              .withIntervalInSeconds(30)
              .repeatForever())
          .build();

      sched.scheduleJob(job3, trigger3);
      _log.info("任务3已调度 - 任务标识: " + job3.getKey() + ", 下次运行时间: " + trigger3.getNextFireTime());

      _log.info("------- 初始化完成 ----------------------");
    }

    // 启动调度器
    _log.info("------- 启动调度器 --------------------");
    sched.start();
    _log.info("------- 调度器已启动 ------------------");

    // 等待 5 分钟以展示集群功能
    _log.info("------- 等待 5 分钟... ----------------");
    _log.info("------- 在此期间，您可以启动其他实例来观察集群行为 ---");
    _log.info("------- 尝试关闭某个实例来测试故障转移功能 ---");
    
    try {
      // 等待 5 分钟
      Thread.sleep(300L * 1000L);
    } catch (Exception e) {
      _log.error("等待过程中发生错误", e);
    }

    _log.info("------- 关闭调度器 --------------------");
    sched.shutdown(true);
    _log.info("------- 调度器已关闭 ------------------");
  }

  /**
   * 程序入口
   * 
   * @param args 命令行参数
   *             - "clearJobs": 清除所有现有任务
   *             - "dontScheduleJobs": 不调度新任务
   */
  public static void main(String[] args) throws Exception {
    boolean clearJobs = false;
    boolean scheduleJobs = true;

    for (String arg : args) {
      if (arg.equalsIgnoreCase("clearJobs")) {
        clearJobs = true;
      } else if (arg.equalsIgnoreCase("dontScheduleJobs")) {
        scheduleJobs = false;
      }
    }

    MySQLClusterExample example = new MySQLClusterExample();
    example.run(clearJobs, scheduleJobs);
  }
}
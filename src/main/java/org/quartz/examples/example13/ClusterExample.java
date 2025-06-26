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
 
package org.quartz.examples.example13;

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
 * Quartz 集群调度演示程序
 * 
 * <p>
 * 本示例将创建大量任务，由默认调度器运行。此示例旨在与多个其他实例（JVM）同时运行，
 * 以演示 JDBCJobStore 的集群功能。
 * </p>
 * 
 * <p>
 * 注意：如果您在 IDE 中运行此示例，应添加系统属性 "org.terracotta.quartz.skipUpdateCheck=true" 
 * 以避免更新检查。
 * </p>
 * 
 * <p>
 * 集群配置要求：
 * - 所有实例都应使用相同的 "org.quartz.scheduler.instanceName"
 * - 集群中的每个实例必须使用不同的 "org.quartz.scheduler.instanceId"
 * - 所有实例必须连接到同一个数据库
 * - 确保系统时间同步
 * </p>
 * 
 * <p>
 * 命令行参数：
 * - "clearJobs": 清除现有的任务和触发器
 * - "dontScheduleJobs": 启动时不调度任务，适用于只处理任务不调度任务的"从节点"
 * </p>
 * 
 * <p>
 * 集群特性演示：
 * - 故障转移：当一个实例崩溃时，其任务会被其他实例接管
 * - 负载均衡：任务会在可用的实例间分布执行
 * - 任务恢复：标记为 requestRecovery 的任务在实例故障后会重新执行
 * </p>
 * 
 * <p>
 * 更多信息请参见 "clustering_readme.txt" 文件。
 * </p>
 * 
 * <p>
 * ClusterExample 执行流程：<br>
 * + 启动调度器<br>
 * + 调度该实例特有的任务<br>
 * + 显示菜单供您选择操作选项<br>
 * </p>
 * 
 * @see SimpleRecoveryJob
 * @see SimpleRecoveryStatefulJob
 * @author James House
 */
public class ClusterExample {

  private static Logger _log = LoggerFactory.getLogger(ClusterExample.class);

  /**
   * 运行集群调度演示
   * 
   * @param inClearJobs 是否清除现有的任务和触发器
   * @param inScheduleJobs 是否调度新任务
   * @throws Exception 如果调度器操作失败
   */
  public void run(boolean inClearJobs, boolean inScheduleJobs) throws Exception {

    // 首先获取调度器的引用
    SchedulerFactory sf = new StdSchedulerFactory();
    Scheduler sched = sf.getScheduler();

    if (inClearJobs) {
      _log.warn("***** 删除现有的任务/触发器 *****");
      sched.clear();
    }

    _log.info("------- 初始化完成 -----------");

    if (inScheduleJobs) {

      _log.info("------- 调度任务 -------------------");

      String schedId = sched.getSchedulerInstanceId();

      int count = 1;

      // 创建第一个简单恢复任务
      // 使用调度器实例ID作为组名，便于在日志中区分不同实例调度的任务
      JobDetail job = newJob(SimpleRecoveryJob.class)
          .withIdentity("job_" + count, schedId) // 任务标识：job_1，组名为实例ID
          .requestRecovery() // 请求恢复：如果调度器崩溃时任务正在执行，重启后会重新执行
          .build();

      SimpleTrigger trigger = newTrigger().withIdentity("triger_" + count, schedId)
          .startAt(futureDate(1, IntervalUnit.SECOND))
          .withSchedule(simpleSchedule().withRepeatCount(20).withIntervalInSeconds(5)).build();

      _log.info(job.getKey() + " 将在 " + trigger.getNextFireTime() + " 运行，重复 "
                + trigger.getRepeatCount() + " 次，每隔 " + trigger.getRepeatInterval() / 1000 + " 秒");
      sched.scheduleJob(job, trigger);

      count++;

      // 创建第二个简单恢复任务
      job = newJob(SimpleRecoveryJob.class)
          .withIdentity("job_" + count, schedId) // 任务标识：job_2，组名为实例ID
          .requestRecovery() // 请求恢复：确保故障转移时任务能被其他实例接管
          .build();

      trigger = newTrigger().withIdentity("triger_" + count, schedId).startAt(futureDate(2, IntervalUnit.SECOND))
          .withSchedule(simpleSchedule().withRepeatCount(20).withIntervalInSeconds(5)).build();

      _log.info(job.getKey() + " 将在 " + trigger.getNextFireTime() + " 运行，重复 "
                + trigger.getRepeatCount() + " 次，每隔 " + trigger.getRepeatInterval() / 1000 + " 秒");
      sched.scheduleJob(job, trigger);

      count++;

      // 创建有状态的恢复任务
      // 有状态任务会自动持久化 JobDataMap，且同一时间只能有一个实例执行
      job = newJob(SimpleRecoveryStatefulJob.class)
          .withIdentity("job_" + count, schedId) // 任务标识：job_3，组名为实例ID
          .requestRecovery() // 请求恢复：有状态任务的恢复更加重要，确保数据一致性
          .build();

      trigger = newTrigger().withIdentity("triger_" + count, schedId).startAt(futureDate(1, IntervalUnit.SECOND))
          .withSchedule(simpleSchedule().withRepeatCount(20).withIntervalInSeconds(3)).build();

      _log.info(job.getKey() + " 将在 " + trigger.getNextFireTime() + " 运行，重复 "
                + trigger.getRepeatCount() + " 次，每隔 " + trigger.getRepeatInterval() / 1000 + " 秒");
      sched.scheduleJob(job, trigger);

      count++;

      // 创建第三个简单恢复任务（4秒间隔）
      job = newJob(SimpleRecoveryJob.class)
          .withIdentity("job_" + count, schedId) // 任务标识：job_4，组名为实例ID
          .requestRecovery() // 请求恢复：保证集群故障转移时任务的连续性
          .build();

      trigger = newTrigger().withIdentity("triger_" + count, schedId).startAt(futureDate(1, IntervalUnit.SECOND))
          .withSchedule(simpleSchedule().withRepeatCount(20).withIntervalInSeconds(4)).build();

      _log.info(job.getKey() + " 将在 " + trigger.getNextFireTime() + " 运行，重复次数: " + trigger.getRepeatCount()
                + "，间隔: " + trigger.getRepeatInterval() + " 毫秒");
      sched.scheduleJob(job, trigger);

      count++;

      // 创建第四个简单恢复任务（4.5秒间隔）
      job = newJob(SimpleRecoveryJob.class)
          .withIdentity("job_" + count, schedId) // 任务标识：job_5，组名为实例ID
          .requestRecovery() // 请求恢复：确保任务在集群环境中的高可用性
          .build();

      trigger = newTrigger().withIdentity("triger_" + count, schedId).startAt(futureDate(1, IntervalUnit.SECOND))
          .withSchedule(simpleSchedule().withRepeatCount(20).withIntervalInMilliseconds(4500L)).build();

      _log.info(job.getKey() + " 将在 " + trigger.getNextFireTime() + " 运行，重复次数: " + trigger.getRepeatCount()
                + "，间隔: " + trigger.getRepeatInterval() + " 毫秒");
      sched.scheduleJob(job, trigger);
    }

    // 任务只有在调用 start() 方法后才开始触发执行
    _log.info("------- 启动调度器 ---------------");
    sched.start();
    _log.info("------- 调度器已启动 ----------------");

    _log.info("------- 等待一小时运行... ----------");
    try {
      Thread.sleep(3600L * 1000L);
    } catch (Exception e) {
      //
    }

    _log.info("------- 关闭调度器 --------------------");
    sched.shutdown();
    _log.info("------- 调度器已关闭 ----------------");
  }

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

    ClusterExample example = new ClusterExample();
    example.run(clearJobs, scheduleJobs);
  }
}

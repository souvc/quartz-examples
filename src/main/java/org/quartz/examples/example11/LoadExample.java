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
 
package org.quartz.examples.example11;

import static org.quartz.DateBuilder.futureDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerMetaData;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 高负载任务调度演示类
 * 
 * 本示例演示如何使用 Quartz 调度器处理大量任务。程序会创建指定数量的任务（默认500个），
 * 并通过线程池控制并发执行数量，展示 Quartz 在高负载场景下的性能表现。
 * 
 * 主要功能：
 * - 批量创建大量任务（可配置数量）
 * - 为每个任务设置随机延迟时间模拟实际工作
 * - 任务按时间间隔依次启动，避免系统压力过大
 * - 支持任务恢复功能（requestRecovery）
 * - 提供详细的执行统计信息
 * 
 * @author James House, Bill Kratzer
 */
public class LoadExample {

  /** 要创建的任务数量，默认为500个 */
  private int _numberOfJobs = 500;

  /**
   * 构造函数
   * 
   * @param inNumberOfJobs 要创建和调度的任务数量
   */
  public LoadExample(int inNumberOfJobs) {
    _numberOfJobs = inNumberOfJobs;
  }

  /**
   * 运行高负载任务调度演示
   * 
   * 此方法执行以下步骤：
   * 1. 初始化调度器
   * 2. 批量创建指定数量的任务
   * 3. 为每个任务设置随机延迟和触发器
   * 4. 启动调度器并运行5分钟
   * 5. 关闭调度器并输出统计信息
   * 
   * @throws Exception 如果调度器操作失败
   */
  public void run() throws Exception {
    Logger log = LoggerFactory.getLogger(LoadExample.class);

    // 首先获取调度器的引用
    SchedulerFactory sf = new StdSchedulerFactory();
    Scheduler sched = sf.getScheduler();

    log.info("------- 初始化完成 -----------");

    // 批量创建并调度指定数量的任务
    for (int count = 1; count <= _numberOfJobs; count++) {
      // 创建任务详情，设置任务恢复功能
      // requestRecovery() 要求调度器在调度器意外关闭时重新执行正在进行的任务
      JobDetail job = newJob(SimpleJob.class)
          .withIdentity("job" + count, "group_1")
          .requestRecovery() // 启用任务恢复功能
          .build();

      // 为任务设置随机延迟时间（0-2500毫秒），模拟实际工作负载
      long timeDelay = (long) (java.lang.Math.random() * 2500);
      job.getJobDataMap().put(SimpleJob.DELAY_TIME, timeDelay);

      // 创建触发器，设置任务启动时间
      // 每个任务的启动时间间隔100毫秒，避免同时启动造成系统压力
      Trigger trigger = newTrigger()
          .withIdentity("trigger_" + count, "group_1")
          .startAt(futureDate((10000 + (count * 100)), IntervalUnit.MILLISECOND))
          .build();

      // 将任务和触发器注册到调度器
      sched.scheduleJob(job, trigger);
      
      // 每创建25个任务输出一次进度信息
      if (count % 25 == 0) {
        log.info("...已调度 " + count + " 个任务");
      }
    }

    log.info("------- 启动调度器 ----------------");

    // 启动调度器开始执行任务
    sched.start();

    log.info("------- 调度器已启动 -----------------");

    log.info("------- 等待五分钟让任务执行... -----------");

    // 等待五分钟让任务有机会运行
    // 在实际应用中，调度器通常会持续运行
    try {
      Thread.sleep(300L * 1000L); // 300秒 = 5分钟
    } catch (Exception e) {
      // 忽略中断异常
    }

    // 关闭调度器
    log.info("------- 正在关闭调度器 ---------------------");
    sched.shutdown(true); // true表示等待正在执行的任务完成
    log.info("------- 调度器关闭完成 -----------------");

    // 输出执行统计信息
    SchedulerMetaData metaData = sched.getMetaData();
    log.info("总共执行了 " + metaData.getNumberOfJobsExecuted() + " 个任务。");
  }

  /**
   * 程序入口点
   * 
   * @param args 命令行参数，可选的第一个参数为要创建的任务数量
   * @throws Exception 如果程序执行失败
   */
  public static void main(String[] args) throws Exception {

    // 默认创建500个任务
    int numberOfJobs = 500;
    
    // 如果提供了命令行参数，使用指定的任务数量
    if (args.length == 1) {
      numberOfJobs = Integer.parseInt(args[0]);
    }
    
    // 检查参数数量，如果超过1个则显示使用说明
    if (args.length > 1) {
      System.out.println("使用方法: java " + LoadExample.class.getName() + " [任务数量]");
      return;
    }
    
    // 创建并运行高负载演示实例
    LoadExample example = new LoadExample(numberOfJobs);
    example.run();
  }

}

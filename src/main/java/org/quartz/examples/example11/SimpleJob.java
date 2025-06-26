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

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 简单任务实现类
 * 
 * 这是一个用于高负载演示的简单任务类，会被调度器大量创建和执行。
 * 主要功能：
 * - 输出任务执行信息（开始和结束时间）
 * - 模拟工作负载（通过随机延迟时间）
 * - 演示任务数据映射的使用
 * - 提供详细的执行日志
 * 
 * 在高负载场景中的作用：
 * - 作为性能测试的目标任务
 * - 验证线程池的并发控制能力
 * - 展示任务恢复功能的效果
 * - 提供执行统计的数据来源
 * 
 * @author Bill Kratzer
 */
public class SimpleJob implements Job {

  /** 日志记录器，用于输出任务执行信息 */
  private static Logger _log = LoggerFactory.getLogger(SimpleJob.class);

  /** 任务数据映射中延迟时间的键名 */
  public static final String DELAY_TIME = "delay time";

  /**
   * 空构造函数
   * 
   * Quartz 调度器通过反射机制创建任务实例时需要无参构造函数
   */
  public SimpleJob() {
  }

  /**
   * 任务执行方法
   * 
   * 当与此任务关联的触发器被触发时，调度器会调用此方法执行任务逻辑。
   * 
   * 在高负载演示中的功能：
   * - 输出任务开始执行的详细信息
   * - 从任务数据映射中获取延迟时间
   * - 通过线程休眠模拟实际工作负载
   * - 输出任务完成执行的信息
   * - 为性能统计提供执行时间数据
   * 
   * @param context 任务执行上下文，包含任务和触发器的详细信息
   * @throws JobExecutionException 如果任务执行过程中发生异常
   */
  public void execute(JobExecutionContext context) throws JobExecutionException {

    // 获取任务标识并输出开始执行信息
    JobKey jobKey = context.getJobDetail().getKey();
    _log.info("开始执行任务: " + jobKey + " 执行时间: " + new Date());

    // 从任务数据映射中获取延迟时间，模拟实际工作负载
    long delayTime = context.getJobDetail().getJobDataMap().getLong(DELAY_TIME);
    try {
      Thread.sleep(delayTime); // 休眠指定时间模拟工作
    } catch (Exception e) {
      // 忽略中断异常
    }

    // 输出任务完成执行信息
    _log.info("完成执行任务: " + jobKey + " 完成时间: " + new Date());
  }

}

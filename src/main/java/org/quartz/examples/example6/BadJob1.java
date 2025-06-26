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
 
package org.quartz.examples.example6;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * <p>
 * 一个演示任务执行异常处理的测试任务类
 * </p>
 * 
 * <p>
 * 本任务演示了以下特性：
 * 1. 任务执行异常的抛出和处理
 * 2. 异常后立即重新触发机制
 * 3. 任务数据的动态修复
 * 4. 有状态任务的数据持久化
 * </p>
 * 
 * <p>
 * 关键特性：
 * - @PersistJobDataAfterExecution: 执行后持久化任务数据
 * - @DisallowConcurrentExecution: 禁止并发执行
 * - 支持异常恢复和数据修复
 * - 演示除零异常的处理和恢复
 * </p>
 * 
 * @author Bill Kratzer
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class BadJob1 implements Job {

  // 日志记录器
  private static Logger _log = LoggerFactory.getLogger(BadJob1.class);
  // 计算结果存储变量
  private int calculation;

  /**
   * 空的公共构造函数，用于任务初始化
   * 
   * <p>
   * Quartz 调度器通过反射机制创建任务实例时需要无参构造函数
   * </p>
   */
  public BadJob1() {
  }

  /**
   * <p>
   * 当与此任务关联的 <code>{@link org.quartz.Trigger}</code> 触发时，
   * 由 <code>{@link org.quartz.Scheduler}</code> 调用此方法。
   * </p>
   * 
   * <p>
   * 本方法演示了异常处理机制：
   * 1. 故意触发除零异常（第一次执行）
   * 2. 捕获异常并创建 JobExecutionException
   * 3. 修复导致异常的数据（将除数设为1）
   * 4. 设置立即重新触发标志
   * 5. 抛出异常以触发重新执行
   * </p>
   * 
   * @param context 任务执行上下文，包含任务详情和触发器信息
   * @throws JobExecutionException 如果任务执行过程中发生异常
   */
  public void execute(JobExecutionContext context) throws JobExecutionException {
    // 获取任务的唯一标识键
    JobKey jobKey = context.getJobDetail().getKey();
    // 获取任务数据映射，用于存储和传递参数
    JobDataMap dataMap = context.getJobDetail().getJobDataMap();

    // 从任务数据中获取除数参数
    int denominator = dataMap.getInt("denominator");
    _log.info("---" + jobKey + " 开始执行于 " + new Date() + "，除数为 " + denominator);

    // 故意构造的异常示例：
    // 由于除零错误导致的异常（仅在第一次运行时发生）
    // 这演示了如何处理任务执行中的异常情况
    try {
      // 执行可能导致异常的计算（4815除以除数）
      calculation = 4815 / denominator;
    } catch (Exception e) {
      _log.info("--- 任务执行中发生错误！");
      // 创建 Quartz 专用的任务执行异常
      JobExecutionException e2 = new JobExecutionException(e);

      // 修复除数，确保下次任务运行时不会再次失败
      // 这演示了异常处理中的数据修复机制
      dataMap.put("denominator", "1");

      // 设置立即重新触发标志
      // 这将导致任务立即重新执行，而不是等待下一个调度时间
      e2.setRefireImmediately(true);
      throw e2;
    }

    _log.info("---" + jobKey + " 执行完成于 " + new Date());
  }

}

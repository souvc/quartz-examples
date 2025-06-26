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

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import java.util.Date;

/**
 * 有状态的测试任务类
 * 
 * <p>
 * 这是一个简单的有状态任务，用于演示失火（Misfire）机制。
 * 该任务会故意执行较长时间，以便观察当任务执行时间超过触发间隔时的失火行为。
 * </p>
 * 
 * <p>
 * 关键特性：
 * - @PersistJobDataAfterExecution: 任务执行后持久化任务数据
 * - @DisallowConcurrentExecution: 禁止同一任务的并发执行
 * - 支持通过 JobDataMap 传递执行延迟参数
 * - 维护任务执行次数统计
 * </p>
 * 
 * @author James House
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class StatefulDumbJob implements Job {

  /*
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   * 
   * 常量定义
   * 
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   */

  /** 任务执行次数的参数键名 */
  public static final String NUM_EXECUTIONS  = "NumExecutions";

  /** 任务执行延迟时间的参数键名（毫秒） */
  public static final String EXECUTION_DELAY = "ExecutionDelay";

  /*
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   * 
   * 构造函数
   * 
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   */

  /**
   * 空构造函数，用于任务初始化
   * 
   * <p>
   * Quartz 调度器会使用反射创建任务实例，因此需要提供无参构造函数。
   * </p>
   */
  public StatefulDumbJob() {
  }

  /*
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   * 
   * 接口实现
   * 
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   */

  /**
   * 任务执行方法
   * 
   * <p>
   * 当与此任务关联的触发器被触发时，由 Quartz 调度器调用此方法。
   * 该方法会故意执行较长时间，以便演示失火机制的工作原理。
   * </p>
   * 
   * <p>
   * 执行流程：
   * 1. 获取并更新任务执行次数
   * 2. 记录任务开始执行的日志
   * 3. 根据配置的延迟时间进行休眠
   * 4. 记录任务执行完成的日志
   * 5. 将更新后的执行次数保存到 JobDataMap 中
   * </p>
   * 
   * @param context 任务执行上下文，包含任务和触发器的相关信息
   * @throws JobExecutionException 如果任务执行过程中发生异常
   */
  public void execute(JobExecutionContext context) throws JobExecutionException {
        // 记录任务开始执行的日志，包含任务键和当前时间
        System.err.println("---" + context.getJobDetail().getKey() + " executing.[" + new Date() + "]");

        // 获取任务数据映射，用于存储和传递任务状态信息
        JobDataMap map = context.getJobDetail().getJobDataMap();

        // 获取当前任务的执行次数，如果不存在则初始化为0
        int executeCount = 0;
        if (map.containsKey(NUM_EXECUTIONS)) {
            executeCount = map.getInt(NUM_EXECUTIONS);
        }

        // 执行次数加1
        executeCount++;

        // 将更新后的执行次数保存回任务数据映射中
        // 由于使用了 @PersistJobDataAfterExecution 注解，这个值会被持久化
        map.put(NUM_EXECUTIONS, executeCount);

        // 获取任务执行延迟时间，默认为5秒
        long delay = 5000l;
        if (map.containsKey(EXECUTION_DELAY)) {
            delay = map.getLong(EXECUTION_DELAY);
        }

        try {
            // 模拟长时间运行的任务，休眠指定的延迟时间
            // 这会导致任务执行时间超过触发间隔，从而触发失火机制
            Thread.sleep(delay);
        } catch (Exception ignore) {
            // 忽略中断异常
        }

        // 记录任务执行完成的日志，包含任务键和执行次数
        System.err.println("  " + context.getJobDetail().getKey() + " complete (" + executeCount + ").");
    }

}

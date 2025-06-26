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
 
package org.quartz.examples.example13;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;

/**
 * 有状态的简单恢复任务实现类
 * 
 * <p>
 * 该任务具有与 SimpleRecoveryJob 相同的功能，但它是一个"有状态"的任务，具有以下特性：
 * </p>
 * 
 * <ul>
 * <li><strong>数据持久化</strong>：任务数据（JobDataMap）在每次执行后会自动重新持久化到数据库</li>
 * <li><strong>并发控制</strong>：同一时间只能有一个该任务的实例在执行（@DisallowConcurrentExecution）</li>
 * <li><strong>状态保持</strong>：执行次数等状态信息会在集群间共享和保持</li>
 * </ul>
 * 
 * <p>
 * 注解说明：
 * - @PersistJobDataAfterExecution：确保 JobDataMap 的变更被持久化
 * - @DisallowConcurrentExecution：防止同一任务的多个实例同时执行
 * </p>
 * 
 * <p>
 * 这种有状态的任务特别适用于需要维护执行状态、避免并发冲突的场景，
 * 在集群环境中能够确保数据一致性和任务执行的可靠性。
 * </p>
 * 
 * @see SimpleRecoveryJob
 * @author Bill Kratzer
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class SimpleRecoveryStatefulJob extends SimpleRecoveryJob {

  /**
   * 有状态恢复任务的构造函数
   * 
   * <p>
   * 调用父类构造函数，继承基本的恢复任务功能。
   * 通过注解增强了状态管理和并发控制能力。
   * </p>
   */
  public SimpleRecoveryStatefulJob() {
    super();
  }
}

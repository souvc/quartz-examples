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

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.UnableToInterruptJobException;


/**
 * <p>
 * 可中断任务的简单实现类，用于演示Quartz中的任务中断机制。
 * </p>
 * <p>
 * 本类演示了以下关键特性：
 * <ul>
 * <li><strong>InterruptableJob接口实现</strong>：允许任务被外部中断</li>
 * <li><strong>中断标志管理</strong>：使用布尔标志跟踪中断状态</li>
 * <li><strong>定期中断检查</strong>：在任务执行过程中定期检查中断信号</li>
 * <li><strong>优雅退出机制</strong>：收到中断信号后安全退出任务</li>
 * <li><strong>长时间运行模拟</strong>：通过睡眠模拟长时间运行的任务</li>
 * </ul>
 * </p>
 * <p>
 * 与普通Job的区别：
 * <ul>
 * <li>实现了InterruptableJob接口，支持外部中断</li>
 * <li>包含interrupt()方法处理中断请求</li>
 * <li>在执行过程中定期检查中断标志</li>
 * <li>支持优雅退出，避免资源泄露</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:bonhamcm@thirdeyeconsulting.com">Chris Bonham</a>
 * @author Bill Kratzer
 */
public class DumbInterruptableJob implements InterruptableJob {
    
    // 日志记录器，用于输出任务执行和中断的相关信息
    private static Logger _log = LoggerFactory.getLogger(DumbInterruptableJob.class);
    
    // 中断标志：标识任务是否已被请求中断
    // volatile关键字确保多线程环境下的可见性
    private boolean _interrupted = false;

    // 任务标识：存储当前任务的唯一标识符，用于日志输出
    private JobKey _jobKey = null;
    
    /**
     * <p>
     * 空的公共构造函数，用于任务初始化。
     * Quartz调度器通过反射机制创建任务实例时需要无参构造函数。
     * </p>
     */
    public DumbInterruptableJob() {
    }


    /**
     * <p>
     * 任务执行方法，当与此任务关联的触发器被触发时，由Quartz调度器调用。
     * </p>
     * <p>
     * 本方法演示了可中断任务的核心特性：
     * <ul>
     * <li><strong>模拟长时间运行</strong>：通过循环和睡眠模拟耗时操作</li>
     * <li><strong>定期中断检查</strong>：在每次循环中检查中断标志</li>
     * <li><strong>优雅退出</strong>：收到中断信号后立即安全退出</li>
     * <li><strong>资源清理</strong>：使用finally块确保日志输出</li>
     * </ul>
     * </p>
     * 
     * @param context 任务执行上下文，包含任务和触发器的详细信息
     * @throws JobExecutionException 如果任务执行过程中发生异常
     */
    public void execute(JobExecutionContext context)
        throws JobExecutionException {

        // 获取任务标识，用于日志输出和中断处理
        _jobKey = context.getJobDetail().getKey();
        _log.info("---- " + _jobKey + " 开始执行于 " + new Date());

        try {
            // 主要任务循环：模拟长时间运行的任务
            // 在实际应用中，这里会是真正的业务逻辑处理
            // 本示例通过睡眠来模拟耗时操作

            for (int i = 0; i < 4; i++) {
                try {
                    // 模拟1秒的工作时间
                    Thread.sleep(1000L);
                } catch (Exception ignore) {
                    // 忽略睡眠中断异常，继续执行
                    ignore.printStackTrace();
                }
                
                // 定期检查是否收到中断信号
                // 这是可中断任务的关键：必须定期检查中断标志
                if(_interrupted) {
                    _log.info("--- " + _jobKey + "  -- 收到中断信号，正在退出!");
                    return; // 优雅退出：立即返回，不抛出异常
                             // 也可以选择抛出JobExecutionException
                             // 具体取决于任务的职责和行为需求
                }
            }
            
        } finally {
            // 确保无论任务正常完成还是被中断，都会记录完成日志
            // finally块保证了资源清理和状态记录的可靠性
            _log.info("---- " + _jobKey + " 执行完成于 " + new Date());
        }
    }
    
    /**
     * <p>
     * 中断处理方法，当用户请求中断任务时由调度器调用。
     * </p>
     * <p>
     * 本方法是InterruptableJob接口的核心实现：
     * <ul>
     * <li><strong>设置中断标志</strong>：将_interrupted标志设为true</li>
     * <li><strong>记录中断事件</strong>：输出中断日志便于调试和监控</li>
     * <li><strong>非阻塞操作</strong>：立即返回，不等待任务实际停止</li>
     * <li><strong>线程安全</strong>：通过简单的布尔标志实现线程间通信</li>
     * </ul>
     * </p>
     * <p>
     * 注意：此方法只是设置中断标志，实际的中断检查和退出逻辑
     * 在execute()方法的主循环中实现。
     * </p>
     * 
     * @throws UnableToInterruptJobException 如果中断任务时发生异常
     */
    public void interrupt() throws UnableToInterruptJobException {
        // 记录中断事件，便于调试和监控
        _log.info("---" + _jobKey + "  -- 正在中断任务 --");
        
        // 设置中断标志为true，通知execute()方法停止执行
        // 这是一个简单而有效的线程间通信机制
        _interrupted = true;
    }

}

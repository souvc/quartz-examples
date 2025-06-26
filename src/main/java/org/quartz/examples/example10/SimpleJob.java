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
 
package org.quartz.examples.example10;

import java.util.Date;
import java.util.Set;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简单任务实现类（插件示例专用）
 * 
 * <p>这是一个专为插件功能演示设计的简单任务实现。此任务通过 XML 配置文件
 * 定义和调度，展示了 XML 任务初始化插件的强大功能。任务执行时会输出详细
 * 的执行信息，并处理任务数据映射，便于观察插件系统的工作效果。</p>
 * 
 * <p>功能特性：</p>
 * <ul>
 * <li>XML 配置驱动：通过 quartz_data.xml 文件定义任务属性</li>
 * <li>数据映射处理：支持从 XML 中读取和处理任务数据</li>
 * <li>详细日志输出：显示任务执行的完整信息</li>
 * <li>历史记录支持：配合历史日志插件记录执行历史</li>
 * <li>结果设置功能：演示任务执行结果的设置</li>
 * </ul>
 * 
 * <p>在插件示例中的作用：</p>
 * <ul>
 * <li>作为 XML 插件加载的目标任务</li>
 * <li>验证插件系统的任务创建功能</li>
 * <li>展示配置文件与任务实现的分离</li>
 * <li>提供历史插件的监听目标</li>
 * <li>演示任务数据映射的使用方式</li>
 * </ul>
 * 
 * @author Bill Kratzer
 */
public class SimpleJob implements Job {

    /** 日志记录器，用于输出任务执行信息和调试信息 */
    private static Logger _log = LoggerFactory.getLogger(SimpleJob.class);

    /**
     * 空构造函数
     * 
     * <p>为 Quartz 调度器通过反射机制创建任务实例提供支持。
     * XML 初始化插件在加载任务定义时会使用此构造函数来实例化任务对象。</p>
     */
    public SimpleJob() {
    }

    /**
     * 任务执行方法
     * 
     * <p>当与此任务关联的 {@code Trigger} 触发时，
     * {@code Scheduler} 会调用此方法来执行任务逻辑。</p>
     * 
     * <p>在插件示例中的功能：</p>
     * <ul>
     * <li>输出详细的任务执行信息，包括任务键和触发器键</li>
     * <li>处理和显示从 XML 配置中传递的任务数据映射</li>
     * <li>设置任务执行结果，供后续处理使用</li>
     * <li>为历史日志插件提供监听目标</li>
     * <li>验证 XML 插件的任务加载和调度功能</li>
     * </ul>
     * 
     * @param context 任务执行上下文，包含任务详情、触发器信息和数据映射
     * @throws JobExecutionException 如果任务执行过程中发生异常
     */
    @SuppressWarnings("unchecked")
    public void execute(JobExecutionContext context)
        throws JobExecutionException {

        // 获取任务的唯一标识键和触发器键
        JobKey jobKey = context.getJobDetail().getKey();
        // 输出任务执行的基本信息，包括任务名称、执行时间和触发器
        _log.info("正在执行任务: " + jobKey + " 执行时间: " + new Date() + ", 触发器: " + context.getTrigger().getKey());
        
        // 检查并处理任务数据映射（JobDataMap）
        // 这些数据可以在 XML 配置文件中定义，用于向任务传递参数
        if(context.getMergedJobDataMap().size() > 0) {
            Set<String> keys = context.getMergedJobDataMap().keySet();
            for(String key: keys) {
                String val = context.getMergedJobDataMap().getString(key);
                _log.info(" - 任务数据映射条目: " + key + " = " + val);
            }
        }
        
        // 设置任务执行结果，可以被后续的监听器或其他组件获取
        context.setResult("hello");
    }

}

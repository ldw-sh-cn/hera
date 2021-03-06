package io.anyway.hera.jvm;

import io.anyway.hera.collector.MetricsHandler;
import io.anyway.hera.common.MetricsQuota;
import io.anyway.hera.collector.MetricsCollector;
import org.springframework.util.ReflectionUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by yangzz on 16/8/24.
 */
public class CpuCollector implements MetricsCollector {

    private MetricsHandler handler;

    private boolean started= false;

    private Method method;

    public void setHandler(MetricsHandler handler){
        this.handler= handler;
    }

    @Override
    public void doCollect() {
        Map<String,Object> props= new LinkedHashMap<String,Object>();
        //获取操作系统
        OperatingSystemMXBean operatingSystem = ManagementFactory.getOperatingSystemMXBean();
        //获取处理器核数
        props.put("availableProcessors",operatingSystem.getAvailableProcessors());
        //操作系统名称
        props.put("name",operatingSystem.getName());
        //操作系统版本号
        props.put("version",operatingSystem.getVersion());
        //cpu负载值
        if(!started) {
            method = ReflectionUtils.findMethod(operatingSystem.getClass(), "getProcessCpuLoad");
            if(method!= null){
                ReflectionUtils.makeAccessible(method);
            }
            started= true;
        }
        if(method!= null){
            double processCpuLoad= (Double) ReflectionUtils.invokeMethod(method,operatingSystem);
            if(processCpuLoad==0){
                processCpuLoad= 0.00000001;
            }
            props.put("processCpuLoad", processCpuLoad);
        }
        else{
            double loadedAverage= operatingSystem.getSystemLoadAverage();
            props.put("processCpuLoad", loadedAverage);
        }
        //发送采集信息
        handler.handle(MetricsQuota.CPU,null,props);
    }

//    private boolean isSunOsMBean(OperatingSystemMXBean operatingSystem) {
//        String className = operatingSystem.getClass().getName();
//        return "com.sun.management.OperatingSystem".equals(className)
//                || "com.sun.management.UnixOperatingSystem".equals(className)
//                || "sun.management.OperatingSystemImpl".equals(className);
//    }
}

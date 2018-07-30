import com.sun.btrace.annotations.*;
import com.sun.btrace.annotations.Export;

import java.lang.reflect.Field;
import java.util.Map;

import static com.sun.btrace.BTraceUtils.*;

/**
 * @Author: yinchengjian
 * @Description:
 * @Date: 2018/7/30
 * @Modified By:
 */
@BTrace
public class BtraceTest1 {

//    static{
//        println("---------------------------JVM properties:---------------------------");
//        printVmArguments();
//        println("---------------------------System properties:------------------------");
//        printProperties();
//        println("---------------------------OS properties:----------------------------");
//        printEnv();
//    }

    /**
     * @Location Kind.ENTRY:     在进入方法时，调用Btrace脚本
     *              Kind.RETURN:    方法执行完时，调用Btrace脚本，只有把拦截位置定义为Kind.RETURN，才能获取方法的返回结果@Return和执行时间@Duration
     *              Kind.CALL:      分析方法中调用其它方法的执行情况，比如在execute方法中，想获取add方法的执行耗时，必须把where设置成Where.AFTER
     *              Kind.LINE:      通过设置line，可以监控代码是否执行到指定的位置
     */

    /**
     * 监控一个方法的执行时间
     *
     * @param orderId        入参
     * @param probeClassName 类名
     * @param pmn            方法名
     * @param duration       耗时，单位纳秒
     * @param map            结果map
     */
    @OnMethod(clazz = "com.wosai.upay.remit.service.RemitOrderServiceImpl", method = "getRemitOrder", location = @Location(Kind.RETURN))
    public static void printMethodRunTime(String orderId, @ProbeClassName String probeClassName, @ProbeMethodName String pmn, @Duration long duration,
                                          @Return Map map) {
        println("\n监控执行时间-------------start\n");
        println(probeClassName + pmn + ",duration:" + duration / 1000000 + " ms");
        println("input: " + orderId);
        println("output: " + map);
        if (matches("12345", orderId)) {
            println("orderId命中");
        }
        println("\n监控执行时间-------------end\n");
    }

    /**
     * 统计方法的调用次数，且每隔15秒打印调用次数
     */
    @Export
    static long counter;

    @OnMethod(clazz = "com.wosai.upay.remit.service.RemitOrderServiceImpl", method = "getRemitOrder")
    public static void testCountRun() {
        counter++;
    }

    @OnTimer(1000 * 15)
    public static void run() {
        println("\n输出统计次数-------------start\n");
        println("count:" + counter);
        counter = 0;
        println("\n输出统计次数-------------end\n");
    }

    /**
     * 检查代码是否又走到144行
     */
    @OnMethod(clazz = "com.wosai.upay.remit.service.RemitOrderServiceImpl", location = @Location(value = Kind.LINE, line = 144))
    public static void onBind() {
        println("执行到第144行");
    }

    /**
     * 查看对象的实例属性值
     * @param self
     */
    @OnMethod(clazz = "com.wosai.upay.remit.service.RemitOrderServiceImpl", method = "getRemitOrder")
    public static void getSelfValue(@Self Object self) {
        println("\n查看对象的实例属性值-------------start\n");
        Field field = field("com.wosai.upay.remit.service.RemitOrderServiceImpl", "configCenter");
        Object object = get(field, self);
        println(object);
        println("\n查看对象的实例属性值-------------end\n");
    }


    /**
     * 每隔4秒检测是否有死锁产生，并打印产生死锁的相关类信息、对应的代码行、线程信息，代码如下。
     */
    @OnTimer(40000)
    public static void trace6() {
        println("\n检查死锁-------------start\n");
        deadlocks();
        println("\n检查死锁-------------end\n");
    }


    /**
     * 监控hashMap扩容过程
     *
     * @param probeClass
     * @param probeMethod
     * @param self
     * @param newCapacity
     */
    @OnMethod(clazz = "java.util.HashMap", method = "resize",
            location = @Location(value = Kind.CALL, clazz = "/.*/", method = "/.*/"))
    public static void traceMapExpandCapacity(@ProbeClassName String probeClass, @ProbeMethodName String probeMethod,
                                              @Self Object self, int newCapacity) {
        String point = Strings.strcat(Strings.strcat(probeClass, "."), probeMethod);//java/util/HashMap.resize
        Class clazz = classForName("java.util.HashMap");
        println(Strings.strcat(point, "======"));
        //获取实例protected变量
        Map.Entry[] table = (Map.Entry[]) get(field(clazz, "table", true), self);
        int threshold = getInt(field(clazz, "threshold", true), self);
        int size = getInt(field(clazz, "size", true), self);
        println(Strings.strcat("newCapacity:", str(newCapacity)));
        println(Strings.strcat("table.length:", str(table.length)));
        println(Strings.strcat("size:", str(size)));
        println(Strings.strcat("threshold:", str(threshold)));
        println(Strings.strcat(point, "------------"));
    }


    /**
     * 分析哪个方法执行了System.gc()
     */
    @OnMethod(clazz = "java.lang.System", method = "gc", location = @Location(Kind.ENTRY))
    public static void onSystemGC() {
        println("System GC");
        jstack();
    }

}
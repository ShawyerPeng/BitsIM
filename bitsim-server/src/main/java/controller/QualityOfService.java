package controller;

/**
 * 服务质量等级
 * Models the values from <A href="http://www.eclipse.org/paho/files/mqttdoc/Casync/qos.html">the Eclipse
 * reference on the quality of service values</A>.
 */
public enum QualityOfService {
    AT_MOST_ONCE,  // 0
    AT_LEAST_ONCE, // 1
    EXACTLY_ONCE   // 2
}
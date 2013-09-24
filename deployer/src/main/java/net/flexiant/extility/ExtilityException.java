
package net.flexiant.extility;

import javax.xml.ws.WebFault;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.8
 * Generated source version: 2.2
 * 
 */
@WebFault(name = "ExtilityException", targetNamespace = "http://extility.flexiant.net")
public class ExtilityException
    extends Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private ExtilityExceptionInfo faultInfo;

    /**
     * 
     * @param message
     * @param faultInfo
     */
    public ExtilityException(String message, ExtilityExceptionInfo faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     */
    public ExtilityException(String message, ExtilityExceptionInfo faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: net.flexiant.extility.ExtilityExceptionInfo
     */
    public ExtilityExceptionInfo getFaultInfo() {
        return faultInfo;
    }

}

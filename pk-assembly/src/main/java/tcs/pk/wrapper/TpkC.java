/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.1
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package tcs.pk.wrapper;

public class TpkC {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected TpkC(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(TpkC obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  @SuppressWarnings("deprecation")
  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        tpkJniJNI.delete_TpkC(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public TpkC() {
    this(tpkJniJNI.new_TpkC(), true);
  }

  public void init() {
    tpkJniJNI.TpkC_init(swigCPtr, this);
  }

  public void _register(IDemandsCB demandsNotify) {
    tpkJniJNI.TpkC__register(swigCPtr, this, IDemandsCB.getCPtr(demandsNotify), demandsNotify);
  }

  public void newDemands(double mAz, double mEl, double eAz, double eEl, double m3R, double m3T) {
    tpkJniJNI.TpkC_newDemands(swigCPtr, this, mAz, mEl, eAz, eEl, m3R, m3T);
  }

  public void newTarget(double ra, double dec) {
    tpkJniJNI.TpkC_newTarget(swigCPtr, this, ra, dec);
  }

  public void offset(double raO, double decO) {
    tpkJniJNI.TpkC_offset(swigCPtr, this, raO, decO);
  }

}
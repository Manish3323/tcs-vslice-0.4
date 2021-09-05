#pragma once

#include <cstdio>
#include <iostream>
#include "tpk/tpk.h"
#include "ScanTask.h"
#include "csw/csw.h"

// The current position in the current ref sys
typedef struct {
    double a, b;
} CurrentPosition;


// Used to access a limited set of TPK functions from Scala/Java
class TpkC {
public:
    TpkC();

    ~TpkC();

    // Disable copy
    TpkC(TpkC const&) = delete;
    TpkC& operator=(TpkC const&) = delete;

    void init();

    void newDemands(double mcsAzDeg, double mcsElDeg, double eAz, double eEl, double m3RotationDeg, double m3TiltDeg);

    void newICRSTarget(double ra, double dec);
    void newFK5Target(double ra, double dec);
    void newAzElTarget(double ra, double dec);

    void setOffset(double raO, double decO);

    // Gets the current CurrentPosition position from the mount
    CurrentPosition currentPosition();

private:
    // Publish CSW events
    void publishMcsDemand(double az, double el);
    void publishEcsDemand(double base, double cap);
    void publishM3Demand(double rotation, double tilt);

    tpk::TimeKeeper* time;
    tpk::TmtMountVt *mount;
    tpk::TmtMountVt *enclosure;
    tpk::Site *site;
    CswEventServiceContext publisher;
    bool publishDemands = false;
};

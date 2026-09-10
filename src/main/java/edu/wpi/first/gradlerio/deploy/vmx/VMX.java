package edu.wpi.first.gradlerio.deploy.vmx;

import javax.inject.Inject;

import org.gradle.api.Project;

import edu.wpi.first.deployutils.deploy.DeployExtension;
import edu.wpi.first.deployutils.deploy.target.location.SshDeployLocation;
import edu.wpi.first.gradlerio.deploy.FRCExtension;
import edu.wpi.first.gradlerio.deploy.roborio.RoboRIO;
import edu.wpi.first.toolchain.NativePlatforms;

/** VMX-pi Linux ARM64 deployment target. */
public class VMX extends RoboRIO {

    @Inject
    public VMX(String name, Project project, DeployExtension de, FRCExtension frcExtension) {
        super(name, project, de, frcExtension);

        // VMX-pi uses the Linux ARM64 ABI and does not expose a roboRIO image.
        getTargetPlatform().set(NativePlatforms.linuxarm64);
        setCheckImage(false);
        setDirectory("/home/lvuser");
        setVmxAddresses();
    }

    @Override
    public void setTeam(int team) {
        super.setTeam(team);
        setVmxAddresses(team);
    }

    @Override
    public boolean usesBundledJre() {
        return false;
    }

    @Override
    public String getJavaCommand() {
        return "/usr/bin/java";
    }

    @Override
    public String getRuntimeCommandPrefix() {
        // The VMX HAL opens pigpio and SPI, which require root; without sudo the
        // program deploys but fails to initialize the board. `sudo env VAR=...`
        // (not `sudo VAR=... cmd`) is used because sudo's env_reset drops the
        // inherited LD_LIBRARY_PATH otherwise -- this matches the documented
        // manual launch line. Requires passwordless sudo for the `vmx` user, as
        // the robot is started unattended.
        return "sudo env LD_LIBRARY_PATH=/usr/local/lib/vmxpi:/usr/local/frc/third-party/lib:$LD_LIBRARY_PATH ";
    }

    private void setVmxAddresses() {
        setVmxAddresses(0);
    }

    private void setVmxAddresses(int team) {
        getLocations().clear();
        addVmxAddress("vmx");
        addVmxAddress("vmx.local");
        if (team > 0) {
            addVmxAddress("10." + (team / 100) + "." + (team % 100) + ".2");
        }
    }

    private void addVmxAddress(String address) {
        getLocations().create(address, SshDeployLocation.class, location -> {
            location.setAddress(address);
            location.setIpv6(false);
            location.setUser("vmx");
            location.setPassword("");
        });
    }

    @Override
    public String toString() {
        return "VMX[" + getName() + "]";
    }
}

/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2020  huangyuhui <huanghongxun2008@126.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.dutianze.jvm;


import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public enum Architecture {
    X86("x86"),
    X86_64("x86-64"),
    IA32("IA-32"),
    IA64("IA-64"),
    SPARC(),
    SPARCV9("SPARC V9"),
    ARM32(),
    ARM64(),
    MIPS(),
    MIPS64(),
    MIPSEL("MIPSel"),
    MIPS64EL("MIPS64el"),
    PPC("PowerPC"),
    PPC64("PowerPC-64"),
    PPCLE("PowerPC (Little-Endian)"),
    PPC64LE("PowerPC-64 (Little-Endian)"),
    S390(),
    S390X("S390x"),
    RISCV32("RISC-V 32"),
    RISCV64("RISC-V 64"),
    LOONGARCH32("LoongArch32"),
    LOONGARCH64_OW("LoongArch64 (old world)"),
    LOONGARCH64("LoongArch64"),
    UNKNOWN("Unknown");

    private final String checkedName;
    private final String displayName;

    Architecture() {
        this.checkedName = this.toString().toLowerCase(Locale.ROOT);
        this.displayName = this.toString();
    }

    Architecture(String displayName) {
        this.checkedName = this.toString().toLowerCase(Locale.ROOT);
        this.displayName = displayName;
    }

    public String getCheckedName() {
        return checkedName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isX86() {
        return this == X86 || this == X86_64;
    }

    public static final Architecture CURRENT_ARCH;
    public static final Architecture SYSTEM_ARCH;

    public static Architecture parseArchName(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        value = value.trim().toLowerCase(Locale.ROOT);

        switch (value) {
            case "x8664":
            case "x86-64":
            case "x86_64":
            case "amd64":
            case "ia32e":
            case "em64t":
            case "x64":
            case "intel64":
                return X86_64;
            case "x8632":
            case "x86-32":
            case "x86_32":
            case "x86":
            case "i86pc":
            case "i386":
            case "i486":
            case "i586":
            case "i686":
            case "ia32":
            case "x32":
                return X86;
            case "arm64":
            case "aarch64":
                return ARM64;
            case "arm":
            case "arm32":
                return ARM32;
            case "mips64":
                return MIPS64;
            case "mips64el":
                return MIPS64EL;
            case "mips":
            case "mips32":
                return MIPS;
            case "mipsel":
            case "mips32el":
                return MIPSEL;
            case "riscv":
            case "risc-v":
            case "riscv64":
                return RISCV64;
            case "ia64":
            case "ia64w":
            case "itanium64":
                return IA64;
            case "ia64n":
                return IA32;
            case "sparcv9":
            case "sparc64":
                return SPARCV9;
            case "sparc":
            case "sparc32":
                return SPARC;
            case "ppc64":
            case "powerpc64":
                return "little".equals(System.getProperty("sun.cpu.endian")) ? PPC64LE : PPC64;
            case "ppc64le":
            case "powerpc64le":
                return PPC64LE;
            case "ppc":
            case "ppc32":
            case "powerpc":
            case "powerpc32":
                return PPC;
            case "ppcle":
            case "ppc32le":
            case "powerpcle":
            case "powerpc32le":
                return PPCLE;
            case "s390":
                return S390;
            case "s390x":
                return S390X;
            case "loongarch32":
                return LOONGARCH32;
            case "loongarch64": {
                if (VersionNumber.compare(System.getProperty("os.version"), "5.19") < 0) {
                    return LOONGARCH64_OW;
                }
                return LOONGARCH64;
            }
            case "loongarch64_ow": {
                return LOONGARCH64_OW;
            }
            default:
                if (value.startsWith("armv7")) {
                    return ARM32;
                }
                if (value.startsWith("armv8") || value.startsWith("armv9")) {
                    return ARM64;
                }
                return UNKNOWN;
        }
    }

    static {
        CURRENT_ARCH = parseArchName(System.getProperty("os.arch"));

        Architecture sysArch = null;
        if (OperatingSystem.CURRENT_OS == OperatingSystem.WINDOWS) {
            String processorIdentifier = System.getenv("PROCESSOR_IDENTIFIER");
            if (processorIdentifier != null) {
                int idx = processorIdentifier.indexOf(' ');
                if (idx > 0) {
                    sysArch = parseArchName(processorIdentifier.substring(0, idx));
                }
            }
        } else if (OperatingSystem.CURRENT_OS == OperatingSystem.OSX) {
            if (CURRENT_ARCH == X86_64) {
                try {
                    Process process = Runtime.getRuntime()
                                             .exec(new String[]{"/usr/sbin/sysctl", "-n", "sysctl.proc_translated"});
                    if (process.waitFor(3, TimeUnit.SECONDS) && process.exitValue() == 0
                        && "1".equals(
                        IOUtils.readFullyAsString(process.getInputStream(),
                                                  OperatingSystem.NATIVE_CHARSET).trim())) {
                        sysArch = ARM64;
                    }
                } catch (Throwable e) {
                    e.printStackTrace(System.err);
                }
            }
        } else {
            for (String uname : new String[]{
                "/bin/uname",
                "/usr/bin/uname"
            }) {
                if (new File(uname).exists()) {
                    try {
                        Process process = Runtime.getRuntime().exec(new String[]{uname, "-m"});
                        if (process.waitFor(3, TimeUnit.SECONDS) && process.exitValue() == 0) {
                            sysArch = parseArchName(
                                IOUtils.readFullyAsString(process.getInputStream(),
                                                          OperatingSystem.NATIVE_CHARSET).trim());
                        }
                    } catch (Throwable e) {
                        e.printStackTrace(System.err);
                    }
                    break;
                }
            }
        }

        SYSTEM_ARCH = sysArch == null || sysArch == UNKNOWN ? CURRENT_ARCH : sysArch;
    }
}

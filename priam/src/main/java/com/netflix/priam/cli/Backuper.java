package com.netflix.priam.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import com.netflix.priam.IConfiguration;
import com.netflix.priam.ICredential;
import com.netflix.priam.defaultimpl.PriamConfiguration;
import com.netflix.priam.defaultimpl.ClearCredential;
import com.netflix.priam.aws.AWSMembership;
import com.netflix.priam.aws.S3BackupPath;
import com.netflix.priam.aws.S3FileSystem;
import com.netflix.priam.aws.SDBInstanceFactory;
import com.netflix.priam.identity.IMembership;
import com.netflix.priam.identity.IPriamInstanceFactory;
import com.netflix.priam.backup.AbstractBackupPath;
import com.netflix.priam.backup.IBackupFileSystem;
import com.netflix.priam.compress.ICompression;
import com.netflix.priam.compress.SnappyCompression;
import com.netflix.priam.utils.Sleeper;
import com.netflix.priam.utils.ThreadSleeper;
import com.netflix.priam.backup.SnapshotBackup;

class GuiceModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(IConfiguration.class).to(PriamConfiguration.class).asEagerSingleton();
        bind(ICredential.class).to(ClearCredential.class);
        bind(IPriamInstanceFactory.class).to(SDBInstanceFactory.class);
        bind(IMembership.class).to(AWSMembership.class);
        bind(IBackupFileSystem.class).to(S3FileSystem.class);
        bind(AbstractBackupPath.class).to(S3BackupPath.class);
        bind(ICompression.class).to(SnappyCompression.class);
        bind(Sleeper.class).to(ThreadSleeper.class);
    }
}

public class Backuper
{
    private static final Logger logger = LoggerFactory.getLogger(Backuper.class);

    public static void main(String[] args)
    {
        Injector injector = Guice.createInjector(new GuiceModule());
        IConfiguration conf = injector.getInstance(IConfiguration.class);
        conf.intialize();
        S3FileSystem fs = injector.getInstance(S3FileSystem.class);
        SnapshotBackup backuper = injector.getInstance(SnapshotBackup.class);
        try
        {
            backuper.execute();
        } catch (Exception e)
        {
            logger.error("Unable to backup: ", e);
        }
        fs.shutdown();
    }
}
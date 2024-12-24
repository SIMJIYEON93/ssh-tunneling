package com.example.sshtunneling.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import jakarta.annotation.PreDestroy;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;


@Slf4j
@Profile("local")
@Component
@ConfigurationProperties(prefix = "ssh")
@Setter
public class SshTunnelingInitializer {

    private String remoteJumpHost;
    private String user;
    private int sshPort;
    private String privateKey;
    private int databasePort;

    private Session session;

    @PreDestroy
    public void closeSSH() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            log.info("SSH session closed.");
        }
    }

    public Integer buildSshConnection() {

        Integer forwardedPort = null;

        try {
            log.info("{}@{}:{}:{} with privateKey", user, remoteJumpHost, sshPort);

            log.info("start ssh tunneling..");
            JSch jSch = new JSch();

            log.info("creating ssh session");
            jSch.addIdentity(privateKey);
            session = jSch.getSession(user, remoteJumpHost, sshPort);


            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            log.info("complete creating ssh session");

            log.info("start connecting ssh connection");
            session.connect();
            log.info("success connecting ssh connection ");

            log.info("start forwarding");
            forwardedPort = session.setPortForwardingL(0, "localhost", databasePort);
            log.info("successfully connected to database");

        } catch (Exception e) {
            log.error("Failed to set up SSH tunneling", e);
            closeSSH();
        }

        return forwardedPort;
    }
}

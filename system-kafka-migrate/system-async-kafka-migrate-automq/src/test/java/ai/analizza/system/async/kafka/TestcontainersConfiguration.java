package ai.analizza.system.async.kafka;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	private static final Network network = Network.newNetwork();

	@Bean
	public GenericContainer<?> minioContainer() {
		GenericContainer<?> minio = new GenericContainer<>(DockerImageName.parse("minio/minio:latest"))
				.withNetwork(network)
				.withNetworkAliases("minio")
				.withCommand("server /data --console-address :9001")
				.withEnv("MINIO_ROOT_USER", "admin")
				.withEnv("MINIO_ROOT_PASSWORD", "password")
				.withExposedPorts(9000)
				.waitingFor(Wait.forHttp("/minio/health/live").forPort(9000));
		
		minio.start();

		try {
			minio.execInContainer("/bin/sh", "-c", "mc alias set local http://127.0.0.1:9000 admin password && mc mb local/automq-data");
		} catch (Exception e) {
			throw new RuntimeException("Failed to create bucket directory in MinIO", e);
		}

		return minio;
	}

	@Bean
	public GenericContainer<?> minioSetupContainer(GenericContainer<?> minioContainer) {
		GenericContainer<?> mc = new GenericContainer<>(DockerImageName.parse("minio/mc:latest"))
				.withNetwork(network)
				.withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint("/bin/sh"))
				.withCommand("-c", "tail -f /dev/null");
		mc.start();
		try {
			String setupScript = "until (mc alias set local http://minio:9000 admin password); do echo '...waiting...'; sleep 1; done; " +
					"mc rm -r --force local/automq-data || true; " +
					"mc rm -r --force local/automq-ops || true; " +
					"mc mb local/automq-data; " +
					"mc mb local/automq-ops; " +
					"mc anonymous set public local/automq-data; " +
					"mc anonymous set public local/automq-ops;";
			org.testcontainers.containers.Container.ExecResult result = mc.execInContainer("sh", "-c", setupScript);
			if (result.getExitCode() != 0) {
				System.err.println("MC STDOUT: " + result.getStdout());
				System.err.println("MC STDERR: " + result.getStderr());
				throw new RuntimeException("Failed to create buckets");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return mc;
	}

	@Bean
	public GenericContainer<?> autoMqContainer(GenericContainer<?> minioSetupContainer) {
		GenericContainer<?> automq = new GenericContainer<>(DockerImageName.parse("automqinc/automq:1.5.5"))
				.withNetwork(network)
				.withEnv("KAFKA_S3_ACCESS_KEY", "admin")
				.withEnv("KAFKA_S3_SECRET_KEY", "password")
				.withEnv("CLUSTER_ID", "rZdE0DjZSrqy96PXrMUZVw")
				.withCommand("bash", "-c", 
						"/opt/automq/kafka/bin/kafka-server-start.sh /opt/automq/kafka/config/kraft/server.properties " +
						"--override cluster.id=$CLUSTER_ID " +
						"--override node.id=0 " +
						"--override controller.quorum.voters=0@localhost:9093 " +
						"--override controller.quorum.bootstrap.servers=localhost:9093 " +
						"--override listeners=PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093 " +
						"--override advertised.listeners=PLAINTEXT://localhost:9095,CONTROLLER://localhost:9093 " +
						"--override s3.data.buckets='0@s3://automq-data?region=us-east-1&endpoint=http://minio:9000&pathStyle=true' " +
						"--override s3.ops.buckets='1@s3://automq-ops?region=us-east-1&endpoint=http://minio:9000&pathStyle=true' " +
						"--override s3.wal.path='0@s3://automq-data?region=us-east-1&endpoint=http://minio:9000&pathStyle=true'")
				.withExposedPorts(9092)
				.withLogConsumer(new org.testcontainers.containers.output.Slf4jLogConsumer(org.slf4j.LoggerFactory.getLogger("automq-container")))
				.waitingFor(Wait.forListeningPort());
		
		automq.setPortBindings(Collections.singletonList("9095:9092"));
		return automq;
	}

	@Bean
	@ServiceConnection
	MongoDBContainer mongoDbContainer() {
		return new MongoDBContainer(DockerImageName.parse("mongo:latest"));
	}

}

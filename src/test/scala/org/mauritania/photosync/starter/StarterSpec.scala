package org.mauritania.photosync.starter

import java.io.File
import java.net.InetSocketAddress

import com.sun.net.httpserver.HttpServer
import org.mauritania.photosync.olympus.sync.TempDir
import org.specs2.mutable.Specification


class StarterSpec extends Specification with TempDir {

  "The starter" should {

    "throw exception in case of bad arguments" in {
      Starter.main(Array("--bad-argument")) must throwA[IllegalArgumentException]
    }

    "works correctly under normal conditions" in {
      withTmpDir { tmp =>
        val expectedDownloadedFile = new File(tmp, new File("100OLYMP", "OR.ORF").getPath)
        val notExpectedDownloadedFile = new File(tmp, new File("100OLYMP", "VI.AVI").getPath)

        val server = HttpServer.create(new InetSocketAddress(8085), 0)
        server.createContext("/", new HttpCameraMock())
        server.setExecutor(null)
        server.start()

        Starter.main(
          Array(
            "--server-name", "localhost",
            "--server-port", "8085",
            "--file-patterns", "*.ORF",
            "--output-directory", tmp.getAbsolutePath
          )
        )

        server.stop(0)

        expectedDownloadedFile.exists() must beTrue
        notExpectedDownloadedFile.exists() must beFalse
      }
    }

  }

}


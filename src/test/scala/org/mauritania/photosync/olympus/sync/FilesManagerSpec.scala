package org.mauritania.photosync.olympus.sync

import org.specs2.mock.Mockito
import org.specs2.mutable._
import java.io.File
import org.mauritania.photosync.olympus.client.CameraClient
import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets

class FilesManagerSpec extends Specification with Mockito {

  "The files manager" should {

    "correctly tell if a file was correctly downloaded" in {

      // Simulate already downloaded local file
      val localFileSimulatingDownloaded = createTmpFile("photo.", 100L)
      val localFilenameSimulatingDownloaded = localFileSimulatingDownloaded.getName
      val localDirectoryOfDownloads = localFileSimulatingDownloaded.getParentFile

      // Simulate camera telling that such file exists and has the same length as the local file
      val cameraClientMock = mock[CameraClient]
      val remoteFilesMock = List((localFilenameSimulatingDownloaded, localFileSimulatingDownloaded.length))
      cameraClientMock.listFiles().returns(remoteFilesMock)

      // The manager should tell the file's already synchronized/downloaded
      val fm = new FilesManager(cameraClientMock, localDirectoryOfDownloads)
      fm.isDownloaded(localFilenameSimulatingDownloaded, remoteFilesMock) mustEqual true

    }

    "correctly tell if a file was incorrectly downloaded" in {

      // Simulate already downloaded local file
      val localFileSimulatingDownloaded = createTmpFile("photo.", 100L)
      val localFilenameSimulatingDownloaded = localFileSimulatingDownloaded.getName
      val localDirectoryOfDownloads = localFileSimulatingDownloaded.getParentFile

      // Simulate camera telling that such file exists but it has different length than the local file
      val cameraClientMock = mock[CameraClient]
      val remoteFilesMock = List((localFilenameSimulatingDownloaded, localFileSimulatingDownloaded.length - 50))
      cameraClientMock.listFiles().returns(remoteFilesMock)

      // The manager should tell the file's is bad and should be re-downloaded
      val fm = new FilesManager(cameraClientMock, localDirectoryOfDownloads)
      fm.isDownloaded(localFilenameSimulatingDownloaded, remoteFilesMock) mustEqual false

    }

    "correctly tell if a file was not downloaded" in {

      // Simulate empty downloads local directory (no photos syncd)
      val localDirectoryOfDownloads = createTmpDir("output")

      // Simulate camera telling there is one file to be downloaded
      val cameraClientMock = mock[CameraClient]
      val remoteFilesMock = List(("photo.jpg", 100L))
      cameraClientMock.listFiles().returns(remoteFilesMock)

      // The manager should tell the file has not been donwloaded yet
      val fm = new FilesManager(cameraClientMock, localDirectoryOfDownloads)
      fm.isDownloaded("photo.jpg", remoteFilesMock) mustEqual false

    }

    "correctly list locally downloaded files" in {

      // Simulate downloads local directory and some photos
      val localDirectoryOfDownloads = createTmpDir("output")
      touchFile(localDirectoryOfDownloads, "photo1.jpg")
      touchFile(localDirectoryOfDownloads, "photo2.jpg")

      // Simulate camera
      val cameraClientMock = mock[CameraClient]

      // The manager should tell the file's is bad and should be re-downloaded
      val fm = new FilesManager(cameraClientMock, localDirectoryOfDownloads)

      fm.listLocalFiles().sortBy(x => x._1) mustEqual List(("photo1.jpg", 0L),("photo2.jpg", 0L)).sortBy(x => x._1)

    }

  }

  // Helpers
  def touchFile(parent: File, filename: String): File = {
    val f = new File(parent, filename)
    f.createNewFile()
    f.deleteOnExit()

    f
  }

  def createTmpFile(prefix: String, size: Long): File = {
    val file = File.createTempFile(prefix, "tmp")
    file.deleteOnExit()
    Files.write(Paths.get(file.getAbsolutePath()), (" " * size.toInt).getBytes(StandardCharsets.UTF_8))

    file
  }

  def createTmpDir(prefix: String): File = {
    val file = File.createTempFile("test", "tmp")
    file.delete()
    file.mkdir()
    file.deleteOnExit()

    file
  }

  // list what are the remote files from an Olympus OMD E-M10
  // correctly synchronize

}

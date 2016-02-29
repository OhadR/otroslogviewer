package com.ohadr.otros.controller;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DownloadController 
{
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(DownloadController.class.getName());

	private int catalinaHomeIndex;

    @RequestMapping(value = "/operation/downloadLogs", method = RequestMethod.GET)
	protected ModelAndView downloads(HttpServletRequest request, HttpServletResponse response)
	{
		ModelAndView model = new ModelAndView("downloadLogs");
		String treeJSON = null;
		try
		{
			treeJSON = getFileSys();
		}
		catch (IOException e)
		{
			model.addObject("errorMessage", e.getMessage());
			e.printStackTrace();
		}
		model.addObject("treeJSON", treeJSON);
		return model;
	}

	// @RequestMapping(value = "/getFileSys", method = RequestMethod.GET)
	protected @ResponseBody String getFileSys() throws IOException
	{
		// String catalinaHome = System.getenv("CATALINA_HOME");
		String catalinaHome = System.getProperty("catalina.home");

		if (StringUtils.isEmpty(catalinaHome))
		{
			throw new IllegalArgumentException("folder is null or empty.");
		}

		catalinaHomeIndex = catalinaHome.length();
		File root = new File(catalinaHome + File.separator);
		String treeJson = "";
		// String treeJson = addFolder(file, ""); // we dont start from root
		// folder
		if (root.isDirectory())
		{
			File[] files = root.listFiles();
			Arrays.sort(files);
			for (int i = 0; i < files.length; i++)
			{
				File file = files[i];
				if (file.getName().equals("temp")) // filter out temp directory
													// so we dong get infinite
													// loop and huge zise for
													// zip
					continue;
				if (i > 0)
					treeJson += ",";
				treeJson += addFolder(file, treeJson);
			}
		}

		return treeJson;
	}

	private String addFolder(File folderObject, String prevPath) throws IOException // throws
																					// WHOException
	{
		String paths = "";
		if (folderObject.isDirectory())
		{
			log.debug("found folder:{}", folderObject.toString());

			paths += "{title: \'" + folderObject.getName() + "\', isFolder: true, folder: true, key: \'" + folderObject.getAbsolutePath().substring(catalinaHomeIndex).replace("\\", "/") + "\'";

			File[] files = folderObject.listFiles();
			if (files != null)
			{
				log.debug("Found:{}, in Folder:{}", files.length, folderObject.getName());

				for (int i = 0; i < files.length; i++)// (File file : files)
				{
					if (i == 0)
						paths += ",\n children: [\n";
					File file = files[i];
					// Recursive call to add folder.
					if (file.isDirectory())
					{
						if (file.getName().equals("temp")) // filter out temp
															// directory so we
															// dong get infinite
															// loop and huge
															// zise for zip
							continue;
						if (i > 0)
							paths += ",";
						paths += addFolder(file, paths);
						if (i == files.length - 1)
							paths += "]\n";
						continue;
					}

					// paths += file.getAbsolutePath() + "<BR/>";
					if (i > 0)
						paths += ",";

					paths += "{title: \'" + file.getName() + " (" + getFileSize(file) + ")\', key: \'" + file.getAbsolutePath().substring(catalinaHomeIndex).replace("\\", "/") + "\'}\n";
					if (i == files.length - 1)
						paths += "]\n";
				}
			}

			paths += " }\n";
		}
		else
		{
			paths += "{title: \'" + folderObject.getName() + " (" + getFileSize(folderObject) + ")\', key: \'" + folderObject.getAbsolutePath().substring(catalinaHomeIndex).replace("\\", "/") + "\'}\n";
		}

		return paths;
	}

	/*
	 * private String getLocalPath(String path) { String localPath =
	 * path.substring(catalinaHomeIndex); localPath = localPath.replace("\\",
	 * "/"); return localPath; }
	 */

	private String getFileSize(File f)
	{
		if (f.length() == 0)
			return "0";
		long bytes = f.length();

		if (bytes < 1024)
		{
			return bytes + "bytes";
		}
		else
		{
			long kb = bytes / 1024;
			if (kb < 1024)
			{
				return kb + "k";
			}
			else
			{
				long mb = kb / 1024;
				long rem = (kb % 1024) / 100;
				if (rem > 0)
					return mb + "." + rem + "Mb";
				else
					return mb + "Mb";
			}
		}
	}
}

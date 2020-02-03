package com.sinha.id3.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Contains method for reading data from data set file
 */
public class ReadInput {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReadInput.class);

	/*
	 * Read from file specified in path variable. Data read into InputSet class file
	 * Returns List<InputSet>.
	 */
	public static List<InputSet> readFile(String path, List<String> attributes) throws IOException {
		if (StringUtils.isBlank(path)) {
			LOGGER.error("No file path input");
			System.exit(-1);
		}
		File file = null;
		FileReader fileReader = null;
		BufferedReader bufReader = null;
		List<InputSet> inputList = new ArrayList<>();
		try {
			file = new File(path);
			fileReader = new FileReader(file);
			bufReader = new BufferedReader(fileReader);
			String line = StringUtils.EMPTY;
			while (null != (line = bufReader.readLine())) {
				String[] lineArr = line.split(",");
				int i = 1;
				InputSet inputSet = new InputSet();
				Map<String, Number> attributeValues = new HashMap<>();

				// Assumes presence of attribute indicating class value at index = 0
				inputSet.setClassValue(NumberFormat.getInstance().parse(lineArr[0]));
				for (String attribute : attributes) {

					// Stores attribute values as Map<String, Number> or attribute:value pairs
					attributeValues.put(attribute, NumberFormat.getInstance().parse(lineArr[i]));
					i++;
				}
				inputSet.setAttributeValues(attributeValues);
				inputList.add(inputSet);
			}
		} catch (Exception e) {
			LOGGER.error("Exception opening file: {}", path);
			LOGGER.error("Exception: ", e);
			System.exit(-1);
		} finally {
			if (null != bufReader) {
				bufReader.close();
			}
		}
		return inputList;
	}
}

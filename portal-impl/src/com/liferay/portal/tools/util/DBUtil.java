/**
 * Copyright (c) 2000-2007 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.liferay.portal.tools.util;

import com.liferay.portal.kernel.util.StringMaker;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.spring.hibernate.HibernateUtil;
import com.liferay.portal.velocity.VelocityUtil;
import com.liferay.util.FileUtil;
import com.liferay.util.SimpleCounter;
import com.liferay.util.StringUtil;
import com.liferay.util.dao.DataAccess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.DerbyDialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.FirebirdDialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.InterbaseDialect;
import org.hibernate.dialect.JDataStoreDialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle9Dialect;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.SAPDBDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.dialect.SybaseDialect;

/**
 * <a href="DBUtil.java.html"><b><i>View Source</i></b></a>
 *
 * @author Alexander Chow
 *
 */
public abstract class DBUtil {

	public static final int DB_TYPE_DB2 = 1;

	public static final int DB_TYPE_DERBY = 2;

	public static final int DB_TYPE_FIREBIRD = 3;

	public static final int DB_TYPE_INTERBASE = 4;

	public static final int DB_TYPE_JDATASTORE = 5;

	public static final int DB_TYPE_HYPERSONIC = 6;

	public static final int DB_TYPE_MYSQL = 7;

	public static final int DB_TYPE_ORACLE = 8;

	public static final int DB_TYPE_POSTGRESQL = 9;

	public static final int DB_TYPE_SAP = 10;

	public static final int DB_TYPE_SQLSERVER = 11;

	public static final int DB_TYPE_SYBASE = 12;

	public static DBUtil getInstance() {
		DBUtil dbUtil = null;

		Dialect dialect = HibernateUtil.getWrappedDialect();

		if (dialect instanceof DB2Dialect) {
			if (dialect instanceof DerbyDialect) {
				dbUtil = DerbyUtil.getInstance();
			}
			else {
				dbUtil = DB2Util.getInstance();
			}
		}
		else if (dialect instanceof InterbaseDialect) {
			if (dialect instanceof FirebirdDialect) {
				dbUtil = FirebirdUtil.getInstance();
			}
			else {
				dbUtil = InterBaseUtil.getInstance();
			}
		}
		else if (dialect instanceof JDataStoreDialect) {
			dbUtil = JDataStoreUtil.getInstance();
		}
		else if (dialect instanceof HSQLDialect) {
			dbUtil = HypersonicUtil.getInstance();
		}
		else if (dialect instanceof MySQLDialect) {
			dbUtil = MySQLUtil.getInstance();
		}
		else if (dialect instanceof OracleDialect || 
				dialect instanceof Oracle9Dialect) {
			
			dbUtil = OracleUtil.getInstance();
		}
		else if (dialect instanceof PostgreSQLDialect) {
			dbUtil = PostgreSQLUtil.getInstance();
		}
		else if (dialect instanceof SAPDBDialect) {
			dbUtil = SAPUtil.getInstance();
		}
		else if (dialect instanceof SybaseDialect) {
			if (dialect instanceof SQLServerDialect) {
				dbUtil = SQLServerUtil.getInstance();
			}
			else {
				dbUtil = SybaseUtil.getInstance();
			}
		}

		return dbUtil;
	}

	public static DBUtil getInstance(int dbType) {
		DBUtil dbUtil = null;

		if (dbType == DB_TYPE_DB2) {
			dbUtil = DB2Util.getInstance();
		}
		else if (dbType == DB_TYPE_DERBY) {
			dbUtil = DerbyUtil.getInstance();
		}
		else if (dbType == DB_TYPE_FIREBIRD) {
			dbUtil = FirebirdUtil.getInstance();
		}
		else if (dbType == DB_TYPE_INTERBASE) {
			dbUtil = InterBaseUtil.getInstance();
		}
		else if (dbType == DB_TYPE_JDATASTORE) {
			dbUtil = JDataStoreUtil.getInstance();
		}
		else if (dbType == DB_TYPE_HYPERSONIC) {
			dbUtil = HypersonicUtil.getInstance();
		}
		else if (dbType == DB_TYPE_MYSQL) {
			dbUtil = MySQLUtil.getInstance();
		}
		else if (dbType == DB_TYPE_ORACLE) {
			dbUtil = OracleUtil.getInstance();
		}
		else if (dbType == DB_TYPE_POSTGRESQL) {
			dbUtil = PostgreSQLUtil.getInstance();
		}
		else if (dbType == DB_TYPE_SAP) {
			dbUtil = SAPUtil.getInstance();
		}
		else if (dbType == DB_TYPE_SQLSERVER) {
			dbUtil = SQLServerUtil.getInstance();
		}
		else if (dbType == DB_TYPE_SYBASE) {
			dbUtil = SybaseUtil.getInstance();
		}

		return dbUtil;
	}

	public void buildCreateFile(String databaseName) throws IOException {
		buildCreateFile(databaseName, true);
		buildCreateFile(databaseName, false);
	}

	public abstract String buildSQL(String template) throws IOException;

	public void buildSQLFile(String fileName) throws IOException {
		String template = buildTemplate(fileName);

		template = buildSQL(template);

		FileUtil.write(
			"../sql/" + fileName + "/" + fileName + "-" + getServerName() +
				".sql",
			template);
	}

	public void executeSQL(String template) throws IOException, SQLException {
		executeSQL(new String[] {template});
	}

	public void executeSQL(String[] templates)
		throws IOException, SQLException {

		Connection con = null;
		Statement stmt = null;

		try {
			con = HibernateUtil.getConnection();

			stmt = con.createStatement();

			for (int i = 0; i < templates.length; i++) {
				stmt.executeUpdate(buildSQL(templates[i]));
			}
		}
		finally {
			DataAccess.cleanUp(con, stmt);
		}
	}

	protected abstract void buildCreateFile(
			String databaseName, boolean minimal)
		throws IOException;

	protected String[] buildColumnNameTokens(String line) {
		String[] words = StringUtil.split(line, " ");

		if (words.length == 7) {
			words[5] = "not null;";
		}

		String[] template = {
			words[1], words[2], words[3], words[4], words[5]
		};

		return template;
	}

	protected String[] buildColumnTypeTokens(String line) {
		String[] words = StringUtil.split(line, " ");

		String nullable = "";

		if (words.length == 6) {
			nullable = "not null;";
		}
		else if (words.length == 5) {
			nullable = words[4];
		}
		else if (words.length == 4) {
			nullable = "not null;";

			if (words[3].endsWith(";")) {
				words[3] = words[3].substring(0, words[3].length() - 1);
			}
		}

		String[] template = {
			words[1], words[2], "", words[3], nullable
		};

		return template;
	}

	protected String buildTemplate(String fileName)
		throws IOException {

		File file = new File("../sql/" + fileName + ".sql");

		String template = FileUtil.read(file);

		if (fileName.equals("portal") || fileName.equals("portal-minimal") ||
			fileName.equals("update-3.6.0-4.0.0")) {

			BufferedReader br = new BufferedReader(new StringReader(template));

			StringMaker sm = new StringMaker();

			String line = null;

			while ((line = br.readLine()) != null) {
				if (line.startsWith("@include ")) {
					int pos = line.indexOf(" ");

					String includeFileName =
						line.substring(pos + 1, line.length());

					String include = FileUtil.read("../sql/" + includeFileName);

					if (includeFileName.endsWith(".vm")) {
						try {
							include = evaluateVM(include);
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}

					include = convertTimestamp(include);
					include =
						StringUtil.replace(include, TEMPLATE, getTemplate());

					sm.append(include);
					sm.append("\n\n");
				}
				else {
					sm.append(line);
					sm.append("\n");
				}
			}

			br.close();

			template = sm.toString();
		}

		if (fileName.equals("indexes") && (this instanceof SybaseUtil)) {
			template = removeBooleanIndexes(template);
		}

		return template;
	}

	protected String convertTimestamp(String data) {
		String s = null;

		if (this instanceof MySQLUtil) {
			s = StringUtil.replace(data, "SPECIFIC_TIMESTAMP_", "");
		}
		else {
			s = data.replaceAll(
				"SPECIFIC_TIMESTAMP_" + "\\d+", "CURRENT_TIMESTAMP");
		}

		return s;
	}

	protected String evaluateVM(String template) throws Exception {
		Map variables = new HashMap();

		variables.put("counter", new SimpleCounter());

		template = VelocityUtil.evaluate(template, variables);

		// Trim insert statements because it breaks MySQL Query Browser

		BufferedReader br = new BufferedReader(new StringReader(template));

		StringMaker sm = new StringMaker();

		String line = null;

		while ((line = br.readLine()) != null) {
			line = line.trim();

			sm.append(line);
			sm.append("\n");
		}

		br.close();

		template = sm.toString();
		template = StringUtil.replace(template, "\n\n\n", "\n\n");

		return template;
	}

	protected String getMinimalSuffix(boolean minimal) {
		if (minimal) {
			return "-minimal";
		}
		else {
			return StringPool.BLANK;
		}
	}

	protected abstract String getServerName();

	protected abstract String[] getTemplate();

	protected String readSQL(String fileName, String comments, String eol)
		throws IOException {

		BufferedReader br = new BufferedReader(
			new FileReader(new File(fileName)));

		StringMaker sm = new StringMaker();

		String line = null;

		while ((line = br.readLine()) != null) {
			if (!line.startsWith(comments)) {
				line = StringUtil.replace(
					line,
					new String[] {"\n", "\t"},
					new String[] {"", ""});

				if (line.endsWith(";")) {
					sm.append(line.substring(0, line.length() - 1));
					sm.append(eol);
				}
				else {
					sm.append(line);
				}
			}
		}

		br.close();

		return sm.toString();
	}

	protected String removeBooleanIndexes(String data) throws IOException {
		String portalData = FileUtil.read("../sql/portal-tables.sql");

		BufferedReader br = new BufferedReader(new StringReader(data));

		StringMaker sm = new StringMaker();

		String line = null;

		while ((line = br.readLine()) != null) {
			boolean append = true;

			int x = line.indexOf(" on ");

			if (x != -1) {
				int y = line.indexOf(" (", x);

				String table = line.substring(x + 4, y);

				x = y + 2;
				y = line.indexOf(")", x);

				String[] columns = StringUtil.split(line.substring(x, y));

				x = portalData.indexOf("create table " + table + " (");
				y = portalData.indexOf(");", x);

				String portalTableData = portalData.substring(x, y);

				for (int i = 0; i < columns.length; i++) {
					if (portalTableData.indexOf(
							columns[i].trim() + " BOOLEAN") != -1) {

						append = false;

						break;
					}
				}
			}

			if (append) {
				sm.append(line);
				sm.append("\n");
			}
		}

		br.close();

		return sm.toString();
	}

	protected String removeInserts(String data) throws IOException {
		BufferedReader br = new BufferedReader(new StringReader(data));

		StringMaker sm = new StringMaker();

		String line = null;

		while ((line = br.readLine()) != null) {
			if (!line.startsWith("insert into ") &&
				!line.startsWith("update ")) {

				sm.append(line);
				sm.append("\n");
			}
		}

		br.close();

		return sm.toString();
	}

	protected String removeLongInserts(String data) throws IOException {
		BufferedReader br = new BufferedReader(new StringReader(data));

		StringMaker sm = new StringMaker();

		String line = null;

		while ((line = br.readLine()) != null) {
			if (!line.startsWith("insert into Image") &&
				!line.startsWith("insert into JournalArticle") &&
				!line.startsWith("insert into JournalStructure") &&
				!line.startsWith("insert into JournalTemplate") &&
				!line.startsWith("insert into ShoppingItem")) {

				sm.append(line);
				sm.append("\n");
			}
		}

		br.close();

		return sm.toString();
	}

	protected String removeNull(String content) {
		content = StringUtil.replace(content, " not null", " not_null");
		content = StringUtil.replace(content, " null", "");
		content = StringUtil.replace(content, " not_null", " not null");

		return content;
	}

	protected abstract String reword(String data) throws IOException;

	protected static String ALTER_COLUMN_TYPE = "alter_column_type ";

	protected static String ALTER_COLUMN_NAME = "alter_column_name ";

	protected static String DROP_PRIMARY_KEY = "drop primary key";

	protected static String[] REWORD_TEMPLATE = {
		"@table@", "@old-column@", "@new-column@", "@type@", "@nullable@"
	};

	protected static String[] TEMPLATE = {
		"##", "TRUE", "FALSE",
		"'01/01/1970'", "CURRENT_TIMESTAMP",
		" BOOLEAN", " DATE", " DOUBLE",
		" INTEGER", " LONG",
		" STRING", " TEXT", " VARCHAR",
		" IDENTITY", "COMMIT_TRANSACTION"
	};

}
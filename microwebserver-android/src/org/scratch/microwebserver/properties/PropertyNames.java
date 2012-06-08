/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.properties;

public enum PropertyNames
{
	LOGGERNAME(false,PropertyDatatype.STRING)
	{
		public String toString()
		{
			return "microwebserver";
		}
	},
	DATABASE_TYPE(false,PropertyDatatype.STRING)
	{
		public String toString()
		{
			return "microwebserver.database.type";
		}
	},
	ALLOW_DIRLIST(true,PropertyDatatype.BOOLEAN)
	{
		public String toString()
		{
			return "microwebserver.allow.directorylisting";
		}
	},
	PROCESS_HTACCESS(false,PropertyDatatype.BOOLEAN)
	{
		public String toString()
		{
			return "microwebserver.parse.htaccess";
		}
	},
	FOLLOW_SYMLINKS(false,PropertyDatatype.BOOLEAN)
	{
		public String toString()
		{
			return "microwebserver.allow.followsymlinks";
		}
	},

	DATABASE_URL(false,PropertyDatatype.STRING)
	{
		public String toString()
		{
			return "microwebserver.database";
		}
	},
	SERVER_PORT(true,PropertyDatatype.INT)
	{
		public String toString()
		{
			return "microwebserver.port";
		}
	},
	SERVER_PORTREDIRECT80(true,PropertyDatatype.BOOLEAN)
	{
		public String toString()
		{
			return "microwebserver.port.redirect80";
		}
	},

	SERVER_SSLENABLE(false,PropertyDatatype.BOOLEAN)
	{
		public String toString()
		{
			return "microwebserver.sslenable";
		}
	},
	SERVER_PORTSSL(false,PropertyDatatype.INT)
	{
		public String toString()
		{
			return "microwebserver.portssl";
		}
	},
	SERVER_SSLCERT(false,PropertyDatatype.FILE)
	{
		public String toString()
		{
			return "microwebserver.certificate";
		}
	},

	SERVER_WORKERS(true,PropertyDatatype.INT)
	{
		public String toString()
		{
			return "microwebserver.workers";
		}
	},
	SERVER_ROOT(true,PropertyDatatype.FOLDER)
	{
		public String toString()
		{
			return "microwebserver.webroot";
		}
	},
	TOKEN_EXPIRATION(true,PropertyDatatype.INT)
	{
		public String toString()
		{
			return "token.expiration";
		}
	},

	CACHE_PATH(false,PropertyDatatype.FOLDER)
	{
		public String toString()
		{
			return "microwebserver.cachepath";
		}
	},

	DEFAULT_FOLDER_ICON(true,PropertyDatatype.MIXED)
	{
		public String toString()
		{
			return "microwebserver.default.folder.icon";
		}
	},
	DEFAULT_FILE_ICON(true,PropertyDatatype.MIXED)
	{
		public String toString()
		{
			return "microwebserver.default.file.icon";
		}
	},

	INDEX_NAME(true,PropertyDatatype.STRING)
	{
		public String toString()
		{
			return "index";
		}
	},

	MAGICMIME_FILE(false,PropertyDatatype.FILE)
	{
		public String toString()
		{
			return "magicmime.file";
		}
	};
	
	private boolean conf;
	private PropertyDatatype type;

	 private PropertyNames(boolean configurable,PropertyDatatype type)
	 {
	   this.conf=configurable;
	   this.type=type;
	 }
	 
	 public PropertyDatatype getType()
	 {
		 return type;
	 }

	 public boolean isConfigurable()
	 {
	   return conf;
	 }

}

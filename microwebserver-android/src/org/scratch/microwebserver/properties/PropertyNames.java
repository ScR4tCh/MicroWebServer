/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.properties;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

	DATABASES_PATH(false,PropertyDatatype.STRING)
	{
		public String toString()
		{
			return "microwebserver.databases";
		}
	},
	SERVER_PORT(true,PropertyDatatype.INT,new PropertyDependency[]{},new PropertyChecks[]{new PropertyChecks("input>1024 && input<65535","must be > 1024 < 65535")})
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

	SERVER_SSLENABLE(true,PropertyDatatype.BOOLEAN)
	{
		public String toString()
		{
			return "microwebserver.sslenable";
		}
	},
	SERVER_PORTSSL(true,PropertyDatatype.INT,new PropertyDependency[]{new PropertyDependency(SERVER_SSLENABLE,"input")},new PropertyChecks[]{new PropertyChecks("input>1024 && input<65535","must be > 1024 < 65535")})
	{
		public String toString()
		{
			return "microwebserver.portssl";
		}
	},
	SERVER_KEYSTORE(true,PropertyDatatype.FILE,new PropertyDependency[]{new PropertyDependency(SERVER_SSLENABLE,"input")},new PropertyChecks[]{})
	{
		public String toString()
		{
			return "microwebserver.keystore";
		}
	},
	SSL_KEYSTOREPASS(true,PropertyDatatype.PASSWORD,new PropertyDependency[]{new PropertyDependency(SERVER_SSLENABLE,"input")},new PropertyChecks[]{})
	{
		public String toString()
		{
			return "microwebserver.keystore.password";
		}
	},
	SERVER_PORTREDIRECT443(true,PropertyDatatype.BOOLEAN,new PropertyDependency[]{new PropertyDependency(SERVER_SSLENABLE,"input")},new PropertyChecks[]{})
	{
		public String toString()
		{
			return "microwebserver.port.redirect443";
		}
	},
	SERVER_WORKERS(true,PropertyDatatype.INT,new PropertyDependency[]{},new PropertyChecks[]{new PropertyChecks("input>=2 && input<=20","must be >= 2 <= 20")})
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
	TOKEN_EXPIRATION(false,PropertyDatatype.INT)
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
	private Set<PropertyDependency> dependencies = new HashSet<PropertyDependency>();
	private Set<PropertyChecks> checks = new HashSet<PropertyChecks>();

	private PropertyNames(boolean configurable,PropertyDatatype type)
	{
	   this.conf=configurable;
	   this.type=type;
	}
	
	private PropertyNames(boolean configurable,PropertyDatatype type,PropertyDependency[] dependencies,PropertyChecks[] checks)
	{
	   this.conf=configurable;
	   this.type=type;
	   
	   this.dependencies=new HashSet<PropertyDependency>(Arrays.asList(dependencies));
	   this.checks=new HashSet<PropertyChecks>(Arrays.asList(checks));
	}
	
	public Set<PropertyDependency> getDependencies()
	{
		return dependencies;
	}
	
	public boolean depends(PropertyNames p)
	{
		return dependencies.contains(p);
	}
	
	private void addDependency(PropertyDependency pd)
	{
		dependencies.add(pd);
	}
	
	public Set<PropertyChecks> getChecks()
	{
		return checks;
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

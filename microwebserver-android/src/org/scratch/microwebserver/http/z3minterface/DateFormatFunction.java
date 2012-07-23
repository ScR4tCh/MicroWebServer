/**
 * Android Micro Web Server
 * Copyright (C) 2011  ScR4tCh
 * Contact scr4tch@scr4tch.org

 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.scratch.microwebserver.http.z3minterface;

import java.text.SimpleDateFormat;

import net.zeminvaders.lang.Interpreter;
import net.zeminvaders.lang.SourcePosition;
import net.zeminvaders.lang.runtime.ZemObject;
import net.zeminvaders.lang.runtime.ZemString;

public class DateFormatFunction extends MicroWebServerFunction
{	
	/**
	 * Taken from  java.text.SimpleDateFormat API Doc
	 * 
	 * Letter 	Date or Time Component 		Presentation 		Examples
	 *	G 		Era designator 				Text 				AD
	 *	y 		Year 						Year 				1996; 96
	 *	M 		Month in year 				Month 				July; Jul; 07
	 *	w 		Week in year 				Number 				27
	 *	W 		Week in month 				Number 				2
	 *	D 		Day in year 				Number 				189
	 *	d 		Day in month 				Number 				10
	 *	F 		Day of week in month 		Number 				2
	 *	E 		Day in week 				Text 				Tuesday; Tue
	 *	a 		Am/pm marker 				Text 				PM
	 *	H 		Hour in day (0-23) 			Number 				0
	 *	k 		Hour in day (1-24) 			Number 				24
	 *	K 		Hour in am/pm (0-11) 		Number 				0
	 *	h 		Hour in am/pm (1-12) 		Number 				12
	 *	m 		Minute in hour 				Number 				30
	 *	s 		Second in minute 			Number 				55
	 *	S 		Millisecond 				Number 				978
	 *	z 		Time zone 					General time zone 	Pacific Standard Time; PST; GMT-08:00
	 *	Z 		Time zone 					RFC 822 time zone 	-0800
	 */
	
	public DateFormatFunction()
	{
	}

	@Override
	public int compareTo(ZemObject o)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ZemObject eval(Interpreter interpreter,SourcePosition pos)
	{
		String format = interpreter.getVariable("format", pos).toZString().toString();
		long millis = interpreter.getVariable("millis",pos).toNumber(pos).longValue();
		
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		
		return new ZemString(sdf.format(millis));
	}

	@Override
	public int getParameterCount()
	{
		return 2;
	}

	@Override
	public String getParameterName(int index)
	{
		if(index==0)
			return "millis";
		else
			return "format";
	}

}

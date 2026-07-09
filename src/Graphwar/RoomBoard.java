//  Copyright (C) 2011 Lucas Catabriga Rocha <catabriga90@gmail.com>
//    
//  This file is part of Graphwar.
//
//  Graphwar is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  Graphwar is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.

//  You should have received a copy of the GNU General Public License
//  along with Graphwar.  If not, see <http://www.gnu.org/licenses/>.

package Graphwar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ListIterator;

import javax.swing.JPanel;

import GraphServer.Constants;

public class RoomBoard extends JPanel implements MouseMotionListener, MouseListener
{
	private Graphwar graphwar;

	private int width;
	private int minHeight;
	private int height;

	private boolean roomFocused;
	private int focusedRoomNum;

	public static final int FILTER_ALL = 0;
	public static final int FILTER_OCCUPIED = 1;
	public static final int FILTER_EMPTY = 2;
	private int filterMode = FILTER_ALL;

	private final int entryHeight = 20;

	private final Color focusColor = new Color(255,174,0,75);
	private final Color playerCountColor = new Color(19,194,194,75);
	
	public RoomBoard(Graphwar graphwar, int width, int minHeight)
	{
		this.graphwar = graphwar;

		this.width = width;
		this.minHeight = minHeight;
		this.height = minHeight;

		this.setPreferredSize(new Dimension(width, height));
		this.revalidate();

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	public void setFilterMode(int mode)
	{
		this.filterMode = mode;
		this.repaint();
	}

	public int getFilterMode()
	{
		return this.filterMode;
	}

	private boolean matchesFilter(Room room)
	{
		switch(filterMode)
		{
			case FILTER_OCCUPIED:
				return room.getNumPlayers() > 0;
			case FILTER_EMPTY:
				return room.getNumPlayers() == 0;
			default:
				return true;
		}
	}

	private int getFilteredRoomCount()
	{
		int count = 0;
		for(Room room : graphwar.getGlobalClient().getRooms())
		{
			if(matchesFilter(room))
			{
				count++;
			}
		}
		return count;
	}

	private Room getFilteredRoom(int index)
	{
		int count = 0;
		for(Room room : graphwar.getGlobalClient().getRooms())
		{
			if(matchesFilter(room))
			{
				if(count == index)
				{
					return room;
				}
				count++;
			}
		}
		return null;
	}

	private void resize()
	{
		this.height = entryHeight*getFilteredRoomCount();

		if(height < minHeight)
		{
			height = minHeight;
		}

		this.setPreferredSize(new Dimension(width, height));
		this.revalidate();
	}
	
	public void paintComponent(Graphics g)
	{
		resize();

		g.setColor(Color.WHITE);

		g.fillRect(0, 0, this.getWidth()-1, this.getHeight()-1);

		g.setColor(Color.BLACK);

		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setFont(new Font("Sans", Font.BOLD, 14));

		int drawCount = 0;
		for(Room room : graphwar.getGlobalClient().getRooms())
		{
			if(!matchesFilter(room))
			{
				continue;
			}

			int playerCount = room.getNumPlayers();
			int i = drawCount;

			if(roomFocused)
			{
				if(focusedRoomNum == i)
				{
					g2d.setColor(focusColor);
					g2d.fillRect(0, entryHeight*i, width, entryHeight);
				}
			}

			if(playerCount > 0) {
				if (roomFocused && focusedRoomNum == i){
					g2d.setColor(new Color(255,180, 189, 75));
				} else {
					g2d.setColor(playerCountColor);
				}
				g2d.fillRect(0, entryHeight*i, width, entryHeight);
			}

			g2d.setColor(Color.BLACK);
			g2d.drawString(" "+room.getName(), 0, entryHeight*(i+1)-4);

			String mode = "y";

			if(room.getGameMode()==1)
			{
				mode = "y'";
			}
			else if(room.getGameMode()==2)
			{
				mode = "y''";
			}

			g2d.drawString(mode, width-40, entryHeight*(i+1)-7);

			g2d.drawString(playerCount+"/10", width-110, entryHeight*(i+1)-6);

			g2d.drawRect(0, entryHeight*i, width, entryHeight);

			drawCount++;
		}
	}

	public synchronized void mouseMoved(MouseEvent e)
	{		
		if(roomFocused)
		{
			int lastFocus = focusedRoomNum;
			
			focusedRoomNum = e.getY()/entryHeight;
			
			if(focusedRoomNum != lastFocus)
			{
				graphwar.getUI().getScreen(Constants.GLOBAL_ROOM_SCREEN).repaint();
			}
			
		}
		else
		{
			roomFocused = true;
			focusedRoomNum = e.getY()/entryHeight;
			graphwar.getUI().getScreen(Constants.GLOBAL_ROOM_SCREEN).repaint();
		}		
	}

	public void mouseClicked(MouseEvent e) 
	{
		
	}

	public void mouseEntered(MouseEvent e) 
	{
		
	}

	public void mouseExited(MouseEvent e)
	{
		roomFocused = false;
		
		graphwar.getUI().getScreen(Constants.GLOBAL_ROOM_SCREEN).repaint();
	}

	public void mousePressed(MouseEvent e) 
	{
		
	}

	public void mouseReleased(MouseEvent e)
	{
		if(graphwar.getGameData().getGameState() == Constants.NONE)
		{
			int roomNum = e.getY()/entryHeight;

			if(roomNum < getFilteredRoomCount())
			{
				Room room = getFilteredRoom(roomNum);
				if(room == null)
				{
					return;
				}

				((GlobalScreen)graphwar.getUI().getScreen(Constants.GLOBAL_ROOM_SCREEN)).showMessage("连接中...");

				try
				{
					graphwar.joinGame(room.getIp(), room.getPort());
					graphwar.getGameData().addPlayer(graphwar.getGlobalClient().getLocalPlayerName());
					graphwar.getUI().setScreen(Constants.PRE_GAME_SCREEN);
				}
				catch (IOException e1)
				{
					((GlobalScreen)graphwar.getUI().getScreen(Constants.GLOBAL_ROOM_SCREEN)).showMessage("连接失败: " + e1.getMessage());
					graphwar.getGameData().disconnect();
					e1.printStackTrace();
				}
			}
		}
	}

	public void mouseDragged(MouseEvent arg0) {
		// TODO.txt Auto-generated method stub
		
	}
}
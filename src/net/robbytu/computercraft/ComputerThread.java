package net.robbytu.computercraft;

import java.util.concurrent.LinkedBlockingQueue;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

import net.robbytu.computercraft.gui.ComputerBlockGUI;

public class ComputerThread {
	public Thread thread;
	public boolean busy;
	public ComputerBlockGUI gui;
	
	private int id;
	private LinkedBlockingQueue<ComputerTask> tasks;
	
	public ComputerThread(final int id, ComputerBlockGUI gui) {
		this.busy = false;
		this.gui = gui;
		
		this.id = id;
		this.tasks = new LinkedBlockingQueue<ComputerTask>(100);
		
		this.thread = new Thread(new Runnable() {
			public void run()  {
				try {
					LuaTable lua = initLua(id);
					
					while(true) {
						ComputerTask task = tasks.take();
						
						busy = true;
						task.execute(lua, Integer.toString(id));
						busy = false;
					}
				}
				catch(Exception ex) {
					busy = false;
				}
				finally {
					busy = false;
					if(CCMain.instance.ComputerThreads.containsKey(Integer.toString(id))) {
						CCMain.instance.ComputerThreads.remove(Integer.toString(id));
					}
					thread.interrupt();
				}
			}
		});
		
		thread.start();
	}
	
	public LuaTable initLua(final int CID) {
		LuaTable lua = JsePlatform.debugGlobals();
		
		lua.set("collectgarbage", LuaValue.NIL);
		lua.set("dofile", LuaValue.NIL);
		lua.set("load", LuaValue.NIL);
		lua.set("loadfile", LuaValue.NIL);
		lua.set("module", LuaValue.NIL);
		lua.set("require", LuaValue.NIL);
		lua.set("package", LuaValue.NIL);
		lua.set("io", LuaValue.NIL);
		lua.set("os", LuaValue.NIL);
		lua.set("debug", LuaValue.NIL);
		lua.set("print", LuaValue.NIL);
		lua.set("luajava", LuaValue.NIL);
		lua.set("write", LuaValue.NIL);
		
		// Default functions
		lua.set("print", new OneArgFunction() {
            public LuaValue call(LuaValue val) {
            	gui.addEntry(val.toString());

                return LuaValue.NIL;
            }
		});
		
		lua.set("write", new OneArgFunction() {
            public LuaValue call(LuaValue val) {
            	gui.addEntry(val.toString());

                return LuaValue.NIL;
            }
		});
		
		lua.set("writeline", new OneArgFunction() {
            public LuaValue call(LuaValue val) {
            	gui.addEntry(val.toString());

                return LuaValue.NIL;
            }
		});
		
		// color.* functions
		LuaTable color = new LuaTable();
		color.set("byString", new OneArgFunction() {
			public LuaValue call(LuaValue val) {
				String color = val.toString().toUpperCase();
				LuaValue toReturn = LuaValue.valueOf("�f");

				if(color.equals("BLACK")) toReturn = LuaValue.valueOf("�0");
				if(color.equals("DARK_BLUE") || color.equals("DARKBLUE")) toReturn = LuaValue.valueOf("�1");
				if(color.equals("DARK_GREEN") || color.equals("DARKGREEN")) toReturn = LuaValue.valueOf("�2");
				if(color.equals("DARK_AQUA") || color.equals("DARKAQUA")) toReturn = LuaValue.valueOf("�3");
				if(color.equals("DARK_RED") || color.equals("DARKRED")) toReturn = LuaValue.valueOf("�4");
				if(color.equals("DARK_PURPLE") || color.equals("DARKPURPLE")) toReturn = LuaValue.valueOf("�5");
				if(color.equals("GOLD")) toReturn = LuaValue.valueOf("�6");
				if(color.equals("GRAY")) toReturn = LuaValue.valueOf("�7");
				if(color.equals("DARK_GRAY") || color.equals("DARKGRAY")) toReturn = LuaValue.valueOf("�8");
				if(color.equals("BLUE")) toReturn = LuaValue.valueOf("�9");
				if(color.equals("GREEN")) toReturn = LuaValue.valueOf("�a");
				if(color.equals("AQUA")) toReturn = LuaValue.valueOf("�b");
				if(color.equals("RED")) toReturn = LuaValue.valueOf("�c");
				if(color.equals("PURPLE")) toReturn = LuaValue.valueOf("�d");
				if(color.equals("YELLOW")) toReturn = LuaValue.valueOf("�e");
				
				return toReturn;
			}
		});
		lua.set("color", color);
		
		// term.* functions
		LuaTable term = new LuaTable();
		term.set("clear", new ZeroArgFunction() {
			public LuaValue call() {
				gui.clearConsole();
				
				return LuaValue.NIL;
			}
		});
		
		term.set("setInputTip", new OneArgFunction() {
			public LuaValue call(LuaValue val) {
				gui.input.setPlaceholder("�8" + val.toString());
				
				return LuaValue.NIL;
			}
		});

		term.set("setInputPasswordField", new OneArgFunction() {
			public LuaValue call(LuaValue val) {
				gui.input.setPasswordField(val.toboolean());
				
				return LuaValue.NIL;
			}
		});
		
		term.set("getInput", new ZeroArgFunction() {
			public LuaValue call() {
				gui.input.setEnabled(true);
				
				String inp = "";
				while(inp.equals("")) {
					inp = gui.inputBuffer;
					try {
						Thread.sleep(100); // Don't remove. If you do, your CPU is not going to be happy with you
					} catch (InterruptedException e) {
						inp = "ERR_THREAD_INTERUPTION";
					}
				}
				
				gui.inputBuffer = "";
				gui.input.setEnabled(false);
				
				return LuaValue.valueOf(inp);
			}
		});
		lua.set("term", term);
		
		// IO functions
		LuaTable io = new LuaTable();
		io.set("isDir", new OneArgFunction() {
			public LuaValue call(LuaValue val) {
				return LuaValue.valueOf(FileManager.isDir(val.toString(), CID));
			}
		});
		
		io.set("mkdir", new TwoArgFunction() {
			public LuaValue call(LuaValue val, LuaValue val2) {
				return LuaValue.valueOf(FileManager.mkDir(val.toString(), val2.toString(), CID));
			}
		});
		
		io.set("printList", new OneArgFunction() {
			public LuaValue call(LuaValue val) {
				FileManager.printList(val.toString(), CID);
				return LuaValue.NIL;
			}
		});

		io.set("remove", new OneArgFunction() {
			public LuaValue call(LuaValue val) {
				return LuaValue.valueOf(FileManager.rm(val.toString(), CID));
			}
		});
		lua.set("io", io);
		
		return lua;
	}
	
	public void addTask(ComputerTask task) {
		this.tasks.offer(task);
	}
	
	public void stop() {
		if(thread.isAlive()) thread.interrupt();
		
		if(CCMain.instance.ComputerThreads.containsKey(Integer.toString(this.id))) {
			CCMain.instance.ComputerThreads.remove(Integer.toString(this.id));
		}
	}
}
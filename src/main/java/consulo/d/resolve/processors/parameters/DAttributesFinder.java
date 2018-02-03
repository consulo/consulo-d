package consulo.d.resolve.processors.parameters;

import java.io.IOException;

import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

/**
 * from kotlin
 */
public class DAttributesFinder
{
	public enum Visibility
	{
		PUBLIC,
		PRIVATE,
		PROTECTED,
		LOCAL;

		public void write(StubOutputStream stream) throws IOException
		{
			switch(this)
			{
				case PUBLIC:
					stream.writeVarInt(1);
					break;
				case PRIVATE:
					stream.writeVarInt(2);
					break;
				case PROTECTED:
					stream.writeVarInt(3);
					break;
				case LOCAL:
					stream.writeVarInt(4);
					break;
			}
		}

		public static Visibility read(StubInputStream stream) throws IOException
		{
			switch(stream.readVarInt())
			{
				case 1:
					return PUBLIC;
				case 2:
					return PRIVATE;
				case 3:
					return PROTECTED;
				case 4:
					return LOCAL;
			}
			throw new IOException("read illegal int when deserializing Visibility");
		}
	}
}

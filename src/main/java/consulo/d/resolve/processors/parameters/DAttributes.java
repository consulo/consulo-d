package consulo.d.resolve.processors.parameters;

import java.io.IOException;

import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

/**
 * from kotlin
 */
public class DAttributes
{
	private boolean myStatic;
	private DAttributesFinder.Visibility visibility;
	private boolean property;
	private boolean noGC;
	private boolean extern;
	private boolean pure;
	private boolean nothrow;
	private boolean myConst;
	private boolean immutable;
	private boolean myEnum;

	public DAttributes(boolean aStatic,
			DAttributesFinder.Visibility visibility,
			boolean property,
			boolean noGC,
			boolean extern,
			boolean pure,
			boolean nothrow,
			boolean aConst,
			boolean immutable,
			boolean anEnum)
	{
		myStatic = aStatic;
		this.visibility = visibility;
		this.property = property;
		this.noGC = noGC;
		this.extern = extern;
		this.pure = pure;
		this.nothrow = nothrow;
		myConst = aConst;
		this.immutable = immutable;
		myEnum = anEnum;
	}

	public void write(StubOutputStream stream) throws IOException
	{
		stream.writeBoolean(isStatic());
		stream.writeBoolean(isProperty());
		stream.writeBoolean(isNoGC());
		stream.writeBoolean(isExtern());
		stream.writeBoolean(isPure());
		stream.writeBoolean(isNothrow());
		stream.writeBoolean(isConst());
		stream.writeBoolean(isImmutable());
		stream.writeBoolean(isEnum());
		getVisibility().write(stream);
	}

	public static DAttributes read(StubInputStream stream) throws IOException
	{
		boolean static2 = stream.readBoolean();
		boolean property = stream.readBoolean();
		boolean noGC = stream.readBoolean();
		boolean extern = stream.readBoolean();
		boolean pure = stream.readBoolean();
		boolean nothrow = stream.readBoolean();
		boolean const2 = stream.readBoolean();
		boolean immutable = stream.readBoolean();
		boolean enum2 = stream.readBoolean();
		DAttributesFinder.Visibility visibility = DAttributesFinder.Visibility.read(stream);
		return new DAttributes(static2, visibility, property, noGC, extern, pure, nothrow, const2, immutable, enum2);
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}

		DAttributes that = (DAttributes) o;

		if(isStatic() != that.isStatic())
		{
			return false;
		}
		if(isProperty() != that.isProperty())
		{
			return false;
		}
		if(isNoGC() != that.isNoGC())
		{
			return false;
		}
		if(isExtern() != that.isExtern())
		{
			return false;
		}
		if(isPure() != that.isPure())
		{
			return false;
		}
		if(isNothrow() != that.isNothrow())
		{
			return false;
		}
		if(isConst() != that.isConst())
		{
			return false;
		}
		if(isImmutable() != that.isImmutable())
		{
			return false;
		}
		if(isEnum() != that.isEnum())
		{
			return false;
		}
		if(getVisibility() != that.getVisibility())
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = (isStatic() ? 1 : 0);
		result = 31 * result + getVisibility().hashCode();
		result = 31 * result + (isProperty() ? 1 : 0);
		result = 31 * result + (isNoGC() ? 1 : 0);
		result = 31 * result + (isExtern() ? 1 : 0);
		result = 31 * result + (isPure() ? 1 : 0);
		result = 31 * result + (isNothrow() ? 1 : 0);
		result = 31 * result + (isConst() ? 1 : 0);
		result = 31 * result + (isImmutable() ? 1 : 0);
		result = 31 * result + (isEnum() ? 1 : 0);
		return result;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("DAttributes{");
		sb.append("myStatic=").append(isStatic());
		sb.append(", visibility=").append(getVisibility());
		sb.append(", property=").append(isProperty());
		sb.append(", noGC=").append(isNoGC());
		sb.append(", extern=").append(isExtern());
		sb.append(", pure=").append(isPure());
		sb.append(", nothrow=").append(isNothrow());
		sb.append(", myConst=").append(isConst());
		sb.append(", immutable=").append(isImmutable());
		sb.append(", myEnum=").append(isEnum());
		sb.append('}');
		return sb.toString();
	}


	public boolean isStatic()
	{
		return myStatic;
	}

	public DAttributesFinder.Visibility getVisibility()
	{
		return visibility;
	}

	public boolean isProperty()
	{
		return property;
	}

	public boolean isNoGC()
	{
		return noGC;
	}

	public boolean isExtern()
	{
		return extern;
	}

	public boolean isPure()
	{
		return pure;
	}

	public boolean isNothrow()
	{
		return nothrow;
	}

	public boolean isConst()
	{
		return myConst;
	}

	public boolean isImmutable()
	{
		return immutable;
	}

	public boolean isEnum()
	{
		return myEnum;
	}
}

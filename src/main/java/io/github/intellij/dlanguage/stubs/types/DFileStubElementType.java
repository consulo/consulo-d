package io.github.intellij.dlanguage.stubs.types;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiFile;
import com.intellij.psi.StubBuilder;
import com.intellij.psi.stubs.DefaultStubBuilder;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.psi.tree.IStubFileElementType;
import consulo.d.DLanguage;
import io.github.intellij.dlanguage.psi.DlangFile;
import io.github.intellij.dlanguage.stubs.DlangFileStub;

public class DFileStubElementType extends IStubFileElementType<DlangFileStub>
{
	public static final int VERSION = 1;
	public static final DFileStubElementType INSTANCE = new DFileStubElementType();

	public DFileStubElementType()
	{
		super("FILE", DLanguage.INSTANCE);
	}

	@Override
	public StubBuilder getBuilder()
	{
		return new DefaultStubBuilder()
		{
			@NotNull
			@Override
			protected StubElement createStubForFile(@NotNull final PsiFile file)
			{
				if(file instanceof DlangFile)
				{
					return new DlangFileStub((DlangFile) file);
				}
				return super.createStubForFile(file);
			}
		};
	}

	@Override
	public int getStubVersion()
	{
		return VERSION;
	}

	@Override
	public void serialize(@NotNull final DlangFileStub stub, @NotNull final StubOutputStream dataStream) throws IOException
	{
		// todo make files named?
	}

	@NotNull
	@Override
	public DlangFileStub deserialize(@NotNull final StubInputStream dataStream, final StubElement parentStub) throws IOException
	{
		return new DlangFileStub(null);
	}

	@NotNull
	@Override
	public String getExternalId()
	{
		return "d.FILE";
	}
}


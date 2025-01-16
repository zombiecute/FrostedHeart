package com.teammoeg.frostedheart.content.scenario.client.dialog;

import com.teammoeg.frostedheart.content.scenario.client.ClientScene;
import com.teammoeg.chorda.util.client.ClientUtils;
import com.teammoeg.chorda.util.utility.ReferenceValue;

import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;

public class TextInfo {
	public static class SizedReorderingProcessor implements FormattedCharSequence {
		FormattedCharSequence origin;
		int limit = 0;
		boolean isFinished = false;

		public SizedReorderingProcessor(FormattedCharSequence origin) {
			super();
			this.origin = origin;
		}

		public boolean hasText() {
			return limit > 0;
		}

		public FormattedCharSequence asFinished() {
			if (isFinished) return origin;
			return this;
		}
		public int nextSpace() {
			ReferenceValue<Integer> renderTracker = new ReferenceValue<>(0);
			ReferenceValue<Integer> retTracker = new ReferenceValue<>();
			origin.accept((i, s, c) -> {
				if (c != 65533) {
					renderTracker.setVal(renderTracker.getVal() + 1);
				}
				if (renderTracker.getVal() < limit) return true;
				if(Character.isWhitespace(c)) {
					retTracker.setVal(renderTracker.getVal());
				}
				return false;
			});
			retTracker.setIfAbsent(renderTracker::getVal);
			return retTracker.getVal();
		}
		@Override
		public boolean accept(FormattedCharSink p_accept_1_) {
			ReferenceValue<Integer> renderTracker = new ReferenceValue<>(0);
			return origin.accept((i, s, c) -> {
				isFinished = true;
				if (renderTracker.getVal() < limit) {
					p_accept_1_.accept(i, s, c);
				} else {
					isFinished = false;
					return false;
				}
				if (c != 65533) {
					renderTracker.setVal(renderTracker.getVal() + 1);
				}
				return true;
			});
		}

		public void checkIsFinished() {
			origin.accept((i, s, c) -> {
                isFinished = i < limit;
				return true;
			});
		}

		public int getLimit() {
			return limit;
		}

		public void setLimit(int limit) {
			this.limit = limit;
		}

	}
	public Component parent;
	public int line;
	public FormattedCharSequence text;
	public boolean addLimit(int amount,boolean toSpace) {
		if (text instanceof SizedReorderingProcessor) {
			SizedReorderingProcessor t = (SizedReorderingProcessor) text;
			if (!t.isFinished) {
				if(toSpace)
					t.limit=t.nextSpace();
				else
					t.limit+=amount;
				return true;
			}
		}
		return false;
	}

	public TextInfo(Component parent, int line, FormattedCharSequence text) {
		super();
		this.parent = parent;
		this.line = line;
		this.text = text;
		
	}

	public int getMaxLen() {
		return ClientUtils.mc().font.width(ClientScene.toString(getFinished()))+30;
	}
	public int getCurLen() {
		return ClientUtils.mc().font.width(ClientScene.toString(text))+30;
	}
	public FormattedCharSequence asFinished() {
		return (text instanceof SizedReorderingProcessor) ? ((SizedReorderingProcessor) text).asFinished() : text;

	}
	public boolean isFinished() {
		return !(text instanceof SizedReorderingProcessor) || ((SizedReorderingProcessor) text).isFinished;
	}

	public boolean hasText() {
		return !(text instanceof SizedReorderingProcessor) || ((SizedReorderingProcessor) text).hasText();
	}

	public FormattedCharSequence getFinished() {
		return (text instanceof SizedReorderingProcessor) ? ((SizedReorderingProcessor) text).origin : text;
	}
}
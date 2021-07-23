package team.creative.creativecore.common.gui.controls;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.text.CompiledText;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.util.math.geo.Rect;

public class GuiLabel extends GuiControl {
    
    protected CompiledText text;
    public Align align;
    
    public GuiLabel(String name, int x, int y) {
        super(name, x, y, 1, 10);
        if (text == null)
            text = CompiledText.createAnySize();
    }
    
    protected void updateDimension() {
        if (getParent() != null) {
            setWidth(getPreferredWidth());
            setHeight(getPreferredHeight());
        }
    }
    
    public GuiLabel setTitle(Component component) {
        text.setText(component);
        if (getParent() != null)
            initiateLayoutUpdate();
        return this;
    }
    
    public GuiLabel setTitle(List<Component> components) {
        text.setText(components);
        if (getParent() != null)
            initiateLayoutUpdate();
        return this;
    }
    
    @Override
    public void init() {
        updateDimension();
    }
    
    @Override
    public void closed() {}
    
    @Override
    public void tick() {}
    
    @Override
    public ControlFormatting getControlFormatting() {
        return ControlFormatting.TRANSPARENT;
    }
    
    @Override
    @OnlyIn(value = Dist.CLIENT)
    protected void renderContent(PoseStack matrix, Rect rect, int mouseX, int mouseY) {
        text.render(matrix);
    }
    
    @Override
    public void setWidthLayout(int width) {
        int offset = getContentOffset();
        text.setDimension(width - offset * 2, Integer.MAX_VALUE);
        text.calculateDimensions();
        setWidth(text.usedWidth + offset * 2);
        setHeight(text.usedHeight + offset * 2);
    }
    
    @Override
    public int getMinWidth() {
        return 10;
    }
    
    @Override
    public int getPreferredWidth() {
        return text.getTotalWidth() + getContentOffset() * 2;
    }
    
    @Override
    public void setHeightLayout(int height) {
        text.setMaxHeight(height - getContentOffset() * 2);
        setHeight(height);
    }
    
    @Override
    public int getMinHeight() {
        return Minecraft.getInstance().font.lineHeight + getContentOffset() * 2;
    }
    
    @Override
    public int getPreferredHeight() {
        return text.getTotalHeight() + getContentOffset() * 2;
    }
    
}

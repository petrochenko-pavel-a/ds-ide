package com.onpositive.dside.ui;

import java.util.LinkedHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.nebula.widgets.gallery.DefaultGalleryGroupRenderer;
import org.eclipse.nebula.widgets.gallery.DefaultGalleryItemRenderer;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.onpositive.musket_core.IDataSet;
import com.onpositive.semantic.ui.core.Alignment;
import com.onpositive.semantic.ui.core.Point;

public class DataSetGallery extends VisualizerViewer<Control> {

	private LinkedHashMap<It, Image> images;
	private Gallery gallery;

	@Override
	public boolean needsLabel() {
		return false;
	}

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	private static ImageDescriptor zoom = AbstractUIPlugin.imageDescriptorFromPlugin((String) "com.onpositive.dside.ui",
			(String) "/icons/zoom_in.gif");
	private static ImageDescriptor zoomout = AbstractUIPlugin.imageDescriptorFromPlugin((String) "com.onpositive.dside.ui",
			(String) "/icons/zoom_out.gif");
	private static ImageDescriptor clearCo = AbstractUIPlugin.imageDescriptorFromPlugin((String) "com.onpositive.dside.ui",
			(String) "/icons/clear_co.gif");
	private static ImageDescriptor collapse = AbstractUIPlugin.imageDescriptorFromPlugin((String) "com.onpositive.dside.ui",
			(String) "/icons/collapseall.gif");
	private static ImageDescriptor expand = AbstractUIPlugin.imageDescriptorFromPlugin((String) "com.onpositive.dside.ui",
			(String) "/icons/expandall.gif");
	private com.onpositive.dside.ui.GalleryTooltip tt;

	@Override
	protected Control createControl(Composite conComposite) {
		images = new LinkedHashMap<>();
		gallery = new Gallery(conComposite, SWT.V_SCROLL | SWT.VIRTUAL);
		this.tt = new GalleryTooltip((Control)gallery, gallery);
		conComposite.setLayout(new FillLayout());
		DefaultGalleryGroupRenderer gr = new DefaultGalleryGroupRenderer();
		gr.setItemSize(356, 356);
		gr.setMinMargin(3);

		DefaultGalleryItemRenderer ir = new DefaultGalleryItemRenderer();

		gallery.setGroupRenderer(gr);
		gallery.setItemRenderer(ir);
		getLayoutHints().setGridy(true);
		getLayoutHints().setAlignmentHorizontal(Alignment.FILL);
		getLayoutHints().setAlignmentVertical(Alignment.FILL);
		getLayoutHints().setSpan(new Point(3, 1));
		// gallery.setVirtualGroups(true);
		// int len=this.input.getLen();
		gallery.addListener(SWT.SetData, new Listener() {

			public void handleEvent(Event event) {
				GalleryItem item = (GalleryItem) event.item;

				int index;
				if (item.getParentItem() != null) {
					GalleryItem parentItem = item.getParentItem();
					index = parentItem.indexOf(item);
					item.setData(index);
					loadImage(item);
					item.setItemCount(0);
					item.setText("" + index);
				} else {
					index = gallery.indexOf(item);
					IDataSet data = input.get(index);
					int len = data.len();
					item.setItemCount(len);
					item.setExpanded(true);
					item.setText(data.get_name()); // $NON-NLS-1$
					item.setData(data);
				}

				System.out.println("setData index " + index); //$NON-NLS-1$
				// Your image here
				// item.setImage(eclipseImage);
			}

		});

		gallery.setItemCount(input.size());
		fillContextMenu(gallery, gr);
		return gallery;
	}

	private static final int SIZE_STEP = 16;
	private static final int ITEM_HEIGHT = 56;

	private class CopyAction extends Action {

		public CopyAction() {
			super("Copy", SWT.PUSH);
		}

		public void run() {
			GalleryItem[] selection = gallery.getSelection();

			if (selection == null || selection.length == 0) {
				return;
			}
			boolean imageTransfer = ImageTransferWrapper.isAvalable();
			StringBuilder builderData = new StringBuilder();
			String[] fileData = new String[selection.length];
			
			ImageData imageData = null;
			if (imageTransfer) {
				try {
					imageData = selection[0].getImage().getImageData();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			Clipboard clipboard = new Clipboard(Display.getCurrent());
			Object[] contents = new Object[] {  imageData };					
			Transfer[] transfers = imageTransfer
					? new Transfer[] { 
							(Transfer) ImageTransferWrapper.getInstance() }
					: new Transfer[] { FileTransfer.getInstance(), TextTransfer.getInstance() };
			clipboard.setContents(contents, transfers);
			clipboard.dispose();
		}

	}

	private void zoomOut(DefaultGalleryGroupRenderer gr) {
		int itemHeight = gr.getItemHeight();
		int itemWidth = gr.getItemWidth();
		if (itemHeight > 40 && itemWidth > ITEM_HEIGHT) {
			itemHeight -= SIZE_STEP;
			itemWidth -= SIZE_STEP;
		}
		gr.setItemSize(itemWidth, itemHeight);
	}

	private void zoomIn(DefaultGalleryGroupRenderer gr) {
		int itemHeight = gr.getItemHeight();
		int itemWidth = gr.getItemWidth();
		if (itemHeight < 1024) {
			itemHeight += SIZE_STEP;
			itemWidth += SIZE_STEP;
		}
		gr.setItemSize(itemWidth, itemHeight);
	}

	private void fillContextMenu(final Gallery gallery, final DefaultGalleryGroupRenderer gr) {
		MenuManager manager = (MenuManager) this.getActualPopupMenuManager();
		manager.setRemoveAllWhenShown(false);
		manager.add(new CopyAction());
		manager.add((IContributionItem) new Separator());
		Action action = new Action("Zoom In", 1) {

			public void run() {
				zoomIn(gr);
			}
		};
		action.setImageDescriptor(this.zoom);
		manager.add((IAction) action);
		Action action2 = new Action("Zoom Out", 1) {

			public void run() {
				zoomOut(gr);
			}
		};
		action2.setImageDescriptor(this.zoomout);
		manager.add((IAction) action2);
		Menu createContextMenu = manager.createContextMenu((Control) gallery);
		System.out.println(createContextMenu.getItems());
		gallery.setMenu(createContextMenu);
	}

	@Override
	public void dispose() {
		images.values().forEach(i -> {
			i.dispose();
		});
		super.dispose();
		images = null;
	}

	

	protected void loadImage(GalleryItem num) {
		Integer data = (Integer) num.getData();
		IDataSet ds = (IDataSet) num.getParentItem().getData();
		It key = new It(data, ds);
		if (images.containsKey(key)) {
			num.setImage(images.get(key));
		}
		tasks.offerLast(() -> {
			try {
				Object item = null;
				Object id=null;
				try {
					item = ds.item(data);
					id=ds.id(data);
				} catch (Exception e) {
				}
				final String lid=""+id;
				String s = item != null ? item.toString() : null;
				if (images != null) {

					Image image = null;
					if (s != null) {
						image = new Image(Display.getDefault(), s);
					} else {
						image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEC_FIELD_ERROR);
						Image i = new Image(Display.getDefault(), 256, 256);
						GC gc = new GC(i);
						gc.drawImage(image, 80, 122);
						gc.drawText("Error during visualization", 100, 120);
						gc.dispose();
						image = i;
					}
					images.put(key, image);
				}

				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						Integer data = (Integer) num.getData();
						IDataSet ds = (IDataSet) num.getParentItem().getData();
						It key = new It(data, ds);
						if (images == null) {
							return;
						}
						if (images.containsKey(key)) {
							num.setImage(images.get(key));
							num.setText(lid);
						}
					}
				});
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
	}

}

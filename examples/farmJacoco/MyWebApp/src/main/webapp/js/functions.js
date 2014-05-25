jQuery.noConflict();

jQuery(function($) {
  $('#sendRequestMyWebApp').click(function() {
    $.ajax({ type: 'POST', url: '/MyWebApp/getmessage', dataType: 'json' }).done(function(data) {
      $('#result').removeClass('hide bg-danger').addClass('bg-success').text('Got from MyWebApp: ' + data.message);
    }).fail(function(jqXHR, textStatus, errorThrown) {
      $('#result').removeClass('hide bg-success').addClass('bg-danger').text('error: ' + errorThrown);
    });
  });

  $('#sendRequestMyWebService').click(function() {
    $.ajax({ type: 'POST', url: '/MyWebService/getdate', dataType: 'json' }).done(function(data) {
      $('#result').removeClass('hide bg-danger').addClass('bg-success').text('Got from MyWebService: ' + data.date);
    }).fail(function(jqXHR, textStatus, errorThrown) {
      $('#result').removeClass('hide bg-success').addClass('bg-danger').text('error: ' + errorThrown);
    });
  });
});
